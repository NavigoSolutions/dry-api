package com.navigo3.dryapi.server;

import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.exec.json.JsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchResponse;
import com.navigo3.dryapi.core.exec.json.JsonExecutor;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.server.HttpServerSettings.ApiMount;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

public class HttpServer<TContext extends AppContext, TCallContext extends CallContext> {
	private final Undertow server;
	
	private final Map<String, DryApi<TContext, TCallContext>> mounts;

	private HttpServerSettings<TContext, TCallContext> settings;

	public HttpServer(HttpServerSettings<TContext, TCallContext> settings) {
		this.settings = settings;
				
		Builder builder = Undertow.builder();
		
		settings.getHttpInterfaces().forEach(i->builder.addHttpListener(i.getPort(), i.getHost()));
		
		mounts = settings.getApiMounts().stream().collect(Collectors.toMap(ApiMount::getBasePath, ApiMount::getDryApi));
		
		server = builder
			.setHandler(this::handleRequest)
			.build();
	}

	public void start() {
		server.start();
	}

	public void stop() {
		server.stop();
	}
	
	private void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.getRelativePath().equals("/shutdown")) {
			
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
			exchange.setStatusCode(StatusCodes.OK);
	        exchange.getResponseSender().send("Stopping");
	        stop();
	        
	        return;
		}
		
		DryApi<TContext, TCallContext> api = mounts.get(exchange.getRelativePath());
		
		if (api==null) {
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
			exchange.setStatusCode(StatusCodes.NOT_FOUND);
	        exchange.getResponseSender().send("Not found");
	        return;
		}
		
		exchange.getRequestReceiver().receiveFullBytes((ex, data) -> {
			ObjectMapper mapper = JsonUtils.createMapper();
			
			JsonBatchRequest batch = ExceptionUtils.withRuntimeException(()->mapper.readValue(new String(data, "utf-8"), new TypeReference<JsonBatchRequest>() {}));
			
			JsonExecutor<TContext, TCallContext> gate = new JsonExecutor<>(api);
			JsonBatchResponse res = gate.execute(settings.getContextProvider().apply(ex), batch);
			
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
			exchange.setStatusCode(StatusCodes.OK);
	        exchange.getResponseSender().send(ExceptionUtils.withRuntimeException(()->mapper.writeValueAsString(res)));
		}, (ex, e) -> {
			e.printStackTrace();
		});
	}
}
