package com.navigo3.dryapi.client;

import java.io.IOException;
import java.net.URLEncoder;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.exec.json.ImmutableJsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.ImmutableJsonRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchResponse;
import com.navigo3.dryapi.core.exec.json.JsonRequest.RequestType;
import com.navigo3.dryapi.core.util.DryApiConstants;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.JacksonUtils;
import com.navigo3.dryapi.core.util.StringUtils;
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
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RemoteHttpDryApi {
	private static final Logger logger = LoggerFactory.getLogger(RemoteHttpDryApi.class);
	
	private HttpUrl apiUrl;
	private RemoteHttpDryApiSettings settings;
	private List<RequestsBatchData> newTasks = new ArrayList<>();
	private LinkedList<RequestsBatchData> tasks = new LinkedList<>();
	private Set<RequestsBatchData> currentTasks = new HashSet<>();
	
	private Thread thread;
	private volatile boolean shouldStop;
	
	private final OkHttpClient httpClient;
	
	private final AtomicInteger lastId = new AtomicInteger(0);

	private ExtraHeaderParams extraHeaderParams;
	
	public RemoteHttpDryApi(String apiUrlStr, RemoteHttpDryApiSettings settings) {
		apiUrl =  HttpUrl.parse(apiUrlStr);
		
		this.settings = settings;
		
		thread = new Thread(this::handlingLoop);
		
		httpClient = new OkHttpClient.Builder()
			.connectionPool(new ConnectionPool(settings.getMaxExecutedInParallel(), 1, TimeUnit.MINUTES))				
			.build();
	}
	
	public void start(Function<OkHttpClient, ExtraHeaderParams> loginFunc) {
		extraHeaderParams = loginFunc.apply(httpClient);
		
		thread.start();
	}
	
	public void stop(BiConsumer<OkHttpClient, ExtraHeaderParams> logoutFunc) {
		logoutFunc.accept(httpClient, extraHeaderParams);
		
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
		CompletableFuture<ValidationData> resFuture = new CompletableFuture<>();
		
		logger.debug("Validating {} async", method.getQualifiedName());
		
		callSimple(method, input, RequestType.VALIDATE).whenComplete((res, exception)->{
			if (exception!=null) {
				logger.debug("Validate - completed exceptionally");
				resFuture.completeExceptionally(exception);
			} else {
				logger.debug("Validate - completed normally");
				ValidationData val = res.getResponse().get().getValidation().orElseGet(()->ImmutableValidationData.builder().build());
				resFuture.complete(val);
			}
		});
		
		return resFuture;
	}
	
	public <TInput, TOutput> ValidationData validateBlocking(MethodDefinition<TInput, TOutput> method, TInput input) {
		logger.debug("Validate {} blocking", method.getQualifiedName());
		
		ValidationData resVal = ExceptionUtils.withRuntimeException(()->validateAsync(method, input, (res)->{}).get());
		
		logger.debug("Validate {} done", method.getQualifiedName());
		
		return resVal;
	}

	public <TInput, TOutput> CompletableFuture<TOutput> executeAsync(MethodDefinition<TInput, TOutput> method, TInput input, Consumer<TOutput> onSuccess) {
		CompletableFuture<TOutput> resFuture = new CompletableFuture<>();
		
		logger.debug("Executing {} async", method.getQualifiedName());
		
		callSimple(method, input, RequestType.EXECUTE).whenComplete((res, exception)->{
			if (exception!=null) {
				logger.debug("Async - completed exceptionally");
				resFuture.completeExceptionally(exception);
			} else {
				logger.debug("Async - completed normally");
				resFuture.complete(res.getOutput().get());
			}
		});
		
		return resFuture;
	}
	
	public <TInput, TOutput> TOutput executeBlocking(MethodDefinition<TInput, TOutput> method, TInput input) {
		logger.debug("Executing {} blocking", method.getQualifiedName());
		
		TOutput resVal = ExceptionUtils.withRuntimeException(()->executeAsync(method, input, (res)->{}).get());
		
		logger.debug("Execution {} done", method.getQualifiedName());
		
		return resVal;
	}
	
	@SuppressWarnings("unchecked")
	public <TInput, TOutput> CompletableFuture<RequestData<TInput,TOutput>> callSimple(MethodDefinition<TInput, TOutput> method, TInput input, RequestType type) {
		CompletableFuture<RequestData<TInput,TOutput>> resFuture = new CompletableFuture<>();
		
		ModifiableRequestData<TInput, TOutput> req = ModifiableRequestData
			.<TInput, TOutput>create()
			.setUuid(UUID.randomUUID().toString())
			.setInput(input)
			.setMethod(method)
			.setRequestType(type);
		
		RequestsBatchData batch = ImmutableRequestsBatchData
			.builder()
			.addRequests(req)
			.id(lastId.getAndIncrement())
			.build();
		
		callRaw(batch).whenComplete((res, exception)->{
			if (exception!=null) {
				logger.debug("Call simple - completed exceptionally");
				resFuture.completeExceptionally(exception);
			} else {
				logger.debug("Call simple - completed normally");
				resFuture.complete(res.getRequests().get(0));
			}
		});

		return resFuture;
	}
	
	public CompletableFuture<RequestsBatchData> callRaw(RequestsBatchData batch) {
		checkStarted();
		
		Validate.notEmpty(batch.getRequests());
		
		synchronized (newTasks) {
			logger.debug("Adding batch");
			
			newTasks.add(batch);
			this.thread.interrupt();
		}
		
		return batch.getFuture();
	}
	
	public RequestsBatchData callBlockingRaw(RequestsBatchData batch) {
		return ExceptionUtils.withRuntimeException(()->callRaw(batch).get());
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
			
			while (!tasks.isEmpty()) {
				if (currentTasks.size()>=settings.getMaxExecutedInParallel()) {
					break;
				}
				
				logger.debug("Currently available {} tasks and {} running tasks", tasks.size(), currentTasks.size());
	
				RequestsBatchData task = tasks.poll();
				
				processTask(task);
			}
			
			try {
				Thread.sleep(Duration.ofMillis(100).toMillis());
			} catch (InterruptedException e) {
				//OKay
			}
		}
	}

	private void processTask(RequestsBatchData requestsBatch) {
		logger.debug("Processing batch {}", requestsBatch.getId());
		
		ExceptionUtils.withRuntimeException(()->{
			Validate.notEmpty(requestsBatch.getRequests());
			
			ObjectMapper objectMapper = JacksonUtils.createJsonMapper();
			
			JsonBatchRequest batch = buildBatch(objectMapper, requestsBatch);
			
			String mime = DryApiConstants.JSON_MIME;
			
			String content = objectMapper.writeValueAsString(batch);
			
			RequestBody body = RequestBody.create(content, MediaType.get(mime));
				
			Builder reqBuilder = new Request.Builder()
				.url(apiUrl)
				.post(body);
			
			extraHeaderParams.getHeaders().forEach((k, v)->{
				reqBuilder.addHeader(k, v);
			});
			
			reqBuilder.addHeader(
				"Cookie",
				extraHeaderParams
					.getCookies()
					.entrySet()
					.stream()
					.map(e->ExceptionUtils.withRuntimeException(()->{
						return StringUtils.subst("{}={}", URLEncoder.encode(e.getKey(), "UTF-8"), URLEncoder.encode(e.getValue(), "UTF-8"));
					}))
					.collect(Collectors.joining("; "))
			);
			
			Request request = reqBuilder.build();
			
			logger.debug("Request prepared");
			
			httpClient.newCall(request).enqueue(new Callback() {
				@SuppressWarnings("unchecked")
				@Override
				public void onResponse(Call call, Response httpResponse) throws IOException {
					logger.debug("Response for batch {}", requestsBatch.getId());
					
					try {
						if (httpResponse.code()!=200 && httpResponse.code()!=400) {
							throw new RuntimeException(StringUtils.subst("Unexpected error code {}. Content:\n{}", httpResponse.code(), httpResponse.body().string()));
						}					
						
						JsonBatchResponse batchResponse = ExceptionUtils.withRuntimeException(()->objectMapper.readValue(httpResponse.body().string(), JsonBatchResponse.class));
	
						Validate.sameSize(batchResponse.getResponses(), requestsBatch.getRequests());
	
						batchResponse.getResponses().forEach(response->{
							@SuppressWarnings("rawtypes")
							Optional<? extends ModifiableRequestData> requestData = requestsBatch
								.getRequests()
								.stream()
								.filter(r->r.getUuid().equals(response.getRequestUuid()))
								.findFirst();
							
							Validate.isPresent(requestData, "There is no response for gived uuid!");
							
							requestData.get().setResponse(response);
							
							if (response.getOutput().isPresent() && !response.getOutput().get().isNull()) {
								Object output = ExceptionUtils
										.withRuntimeException(()->objectMapper.convertValue(response.getOutput().get(), requestData.get().getMethod().getOutputType()));

								requestData.get().setOutput(Optional.of(output));
							}
								
						});
						
						requestsBatch.getFuture().complete(requestsBatch);
					} catch (Throwable t) {
						logger.debug("Exception for batch {} detected", requestsBatch.getId());
						
						requestsBatch.getOnFail().orElse(settings.getGlobalErrorHandler().orElse(Throwable::printStackTrace));
						requestsBatch.getFuture().completeExceptionally(t);
						
						logger.debug("Exception for batch {} processed", requestsBatch.getId());
					}
				}
				
				@Override
				public void onFailure(Call call, IOException e) {
					logger.debug("Failure for batch {} detected", requestsBatch.getId());
					
					requestsBatch.getOnFail().orElse(settings.getGlobalErrorHandler().orElse(Throwable::printStackTrace));
					requestsBatch.getFuture().completeExceptionally(e);
					
					logger.debug("Failure for batch {} processed", requestsBatch.getId());
				}
			});
		});
	}

	@SuppressWarnings("unchecked")
	private JsonBatchRequest buildBatch(ObjectMapper mapper, RequestsBatchData requestsBatch) {
		Validate.notEmpty(requestsBatch.getRequests());
		
		ImmutableJsonBatchRequest.Builder batchBuilder = ImmutableJsonBatchRequest.builder();
		
		requestsBatch.getRequests().forEach(request->{
			
			if (!request.uuidIsSet()) {
				request.setUuid(UUID.randomUUID().toString());
			}

			batchBuilder.addRequests(ImmutableJsonRequest
				.builder()
				.qualifiedName(request.getMethod().getQualifiedName())
				.input(ExceptionUtils.withRuntimeException(()->mapper.valueToTree(request.getInput())))
				.requestType(request.getRequestType())
				.requestUuid(request.getUuid())
				.addAllInputMappings(request.getInputOutputMappings())
				.build()
			);
		});
		
		ImmutableJsonBatchRequest res = batchBuilder.build();
		
		Validate.notEmpty(res.getRequests());
		
		return res;
	}
}
