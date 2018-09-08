package com.navigo3.dryapi.client;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.exec.json.ImmutableJsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.ImmutableJsonRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchResponse;
import com.navigo3.dryapi.core.exec.json.JsonRequest.RequestType;
import com.navigo3.dryapi.core.exec.json.JsonResponse;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.ImmutableValidationData;
import com.navigo3.dryapi.core.validation.ValidationData;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RemoteHttpDryApi {
	
	@Value.Modifiable
	public interface Task<TInput, TOutput, TResult> {
		MethodDefinition<TInput, TOutput> getMethod();
		
		TInput getInput();
		Optional<TOutput> getOutput();
		boolean getOnlyValidate();
		Consumer<TResult> getOnSuccess();
		Consumer<Throwable> getOnFail();
		CompletableFuture<TResult> getFuture();
	}
	
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private HttpUrl apiUrl;
	private RemoteHttpDryApiSettings settings;
	private List<ModifiableTask<?,?,?>> newTasks = new ArrayList<>();
	private LinkedList<ModifiableTask<?,?,?>> tasks = new LinkedList<>();
	private Set<ModifiableTask<?,?,?>> currentTasks = new HashSet<>();
	
	private Thread thread;
	private volatile boolean shouldStop;
	
	private final OkHttpClient httpClient;
	
	public RemoteHttpDryApi(String apiUrlStr, RemoteHttpDryApiSettings settings) {
		apiUrl =  HttpUrl.parse(apiUrlStr);
		
		this.settings = settings;
		
		thread = new Thread(this::handlingLoop);
		
		httpClient = new OkHttpClient.Builder()
			.connectionPool(new ConnectionPool(settings.getMaxExecutedInParallel(), 1, TimeUnit.MINUTES))				
			.build();
	}
	
	public void start() {
		thread.start();
	}
	
	public void stop() {
		shouldStop = true;
		
		while (thread.isAlive()) {
			try {
				Thread.sleep(Duration.ofMillis(100).toMillis());
			} catch (InterruptedException e) {
				//OKay
			}
		}
	}
	
	public <TInput, TOutput> CompletableFuture<ValidationData> validateAsync(MethodDefinition<TInput, TOutput> method, TInput input, Consumer<ValidationData> onSuccess) {
		checkStarted();
		
		ModifiableTask<TInput, TOutput, ValidationData> task = ModifiableTask
			.<TInput, TOutput, ValidationData>create()
			.setMethod(method)
			.setOnlyValidate(true)
			.setInput(input)
			.setOnSuccess(onSuccess)
			.setOnFail(settings.getGlobalErrorHandler().orElse((t)->t.printStackTrace()))
			.setFuture(new CompletableFuture<>());
		
		synchronized (newTasks) {
			newTasks.add(task);
			this.thread.interrupt();
		}
		
		return task.getFuture();
	}
	
	public <TInput, TOutput> ValidationData validateBlocking(MethodDefinition<TInput, TOutput> method, TInput input) {
		return ExceptionUtils.withRuntimeException(()->validateAsync(method, input, (res)->{}).get());
	}

	public <TInput, TOutput> CompletableFuture<TOutput> executeAsync(MethodDefinition<TInput, TOutput> method, TInput input, Consumer<TOutput> onSuccess) {
		checkStarted();
		
		ModifiableTask<TInput, TOutput, TOutput> task = ModifiableTask
			.<TInput, TOutput, TOutput>create()
			.setMethod(method)
			.setOnlyValidate(false)
			.setInput(input)
			.setOnSuccess(onSuccess)
			.setOnFail(settings.getGlobalErrorHandler().orElse((t)->t.printStackTrace()))
			.setFuture(new CompletableFuture<>());
		
		synchronized (newTasks) {
			newTasks.add(task);
			this.thread.interrupt();
		}
		
		return task.getFuture();
	}
	
	public <TInput, TOutput> TOutput executeBlocking(MethodDefinition<TInput, TOutput> method, TInput input) {
		return ExceptionUtils.withRuntimeException(()->executeAsync(method, input, (res)->{}).get());
	}
	
	private void checkStarted() {
		Validate.isTrue(thread.isAlive() && !shouldStop, "Please start this API first by calling start() and do not forget shutdown by stop()!");
	}
	
	private void handlingLoop() {
		while (!shouldStop) {
			
			synchronized (newTasks) {
				tasks.addAll(newTasks);
				newTasks.clear();
			}
			
			while (!tasks.isEmpty() && currentTasks.size()<settings.getMaxExecutedInParallel()) {
				Task<?, ?, ?> task = tasks.poll();
				
				processTask(task);
			}
			
			try {
				Thread.sleep(Duration.ofMillis(100).toMillis());
			} catch (InterruptedException e) {
				//OKay
			}
		}
	}

	private <TInput, TOutput, TResult> void processTask(Task<TInput, TOutput, TResult> task) {
		ObjectMapper mapper = JsonUtils.createMapper();
		
		JsonBatchRequest batch = ImmutableJsonBatchRequest
			.builder()
			.addRequests(ImmutableJsonRequest
				.builder()
				.qualifiedName(task.getMethod().getQualifiedName())
				.inputJson(ExceptionUtils.withRuntimeException(()->mapper.writeValueAsString(task.getInput())))
				.requestType(task.getOnlyValidate() ? RequestType.VALIDATE : RequestType.EXECUTE)
				.requestUuid(UUID.randomUUID())
				.build()
			)
			.build();
		
		RequestBody body = ExceptionUtils.withRuntimeException(()->RequestBody.create(JSON, mapper.writeValueAsString(batch)));
			
		Request request = new Request.Builder()
			.url(apiUrl)
			.post(body)
			.build();
		
		httpClient.newCall(request).enqueue(new Callback() {
			@SuppressWarnings("unchecked")
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				try {
					JsonBatchResponse batchResponse = ExceptionUtils.withRuntimeException(()->mapper.readValue(response.body().string(), JsonBatchResponse.class));

					JsonResponse responseObj = batchResponse.getResponses().get(0);
					
//					JsonUtils.prettyPrint(responseObj);
					
					if (batchResponse.getOverallSuccess()) {
						if (task.getOnlyValidate()) {
							TResult validRes = (TResult)responseObj.getValidation().orElse(ImmutableValidationData.builder().build());
							
							task.getOnSuccess().accept(validRes);
							
							task.getFuture().complete(validRes);
						} else {
							String json = responseObj.getOutputJson().get();
							
							TResult res = ExceptionUtils.withRuntimeException(()->mapper.readValue(json, task.getMethod().getOutputType()));
							
							task.getOnSuccess().accept(res);
							
							task.getFuture().complete(res);
						} 	
					} else {
						task.getOnFail().accept(new RuntimeException(responseObj.getErrorMessage().orElse("")));
						task.getFuture().completeExceptionally(new RuntimeException(responseObj.getErrorMessage().orElse("")));
					}
					
				} catch (Throwable t) {
					task.getOnFail().accept(t);
					task.getFuture().completeExceptionally(t);
				}
			}
			
			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
				task.getFuture().complete(null);
			}
		});
	}
}
