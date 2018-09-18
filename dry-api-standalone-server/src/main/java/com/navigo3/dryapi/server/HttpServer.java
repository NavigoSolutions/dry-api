package com.navigo3.dryapi.server;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.exec.json.JsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchResponse;
import com.navigo3.dryapi.core.exec.json.JsonExecutor;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.Function3;
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.validation.Validator;
import com.navigo3.dryapi.server.HttpServerSettings.ApiMount;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

public class HttpServer<TAppContext extends AppContext, TCallContext extends CallContext, TValidator extends Validator> {
	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    
	private final Undertow server;
	
	private final Map<String, DryApi<TAppContext, TCallContext, TValidator>> mounts;

	private HttpServerSettings<TAppContext, TCallContext, TValidator> settings;

	private Function3<TAppContext, TCallContext, ObjectPathsTree, TValidator> validatorProvider;

	public HttpServer(HttpServerSettings<TAppContext, TCallContext, TValidator> settings, Function3<TAppContext, TCallContext, ObjectPathsTree, TValidator> validatorProvider) {
		this.settings = settings;
		this.validatorProvider = validatorProvider;
				
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
		logger.debug("Handling request");
		
		if (exchange.getRelativePath().equals("/shutdown")) {
			
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
			exchange.setStatusCode(StatusCodes.OK);
	        exchange.getResponseSender().send("Stopping");
	        stop();
	        
	        return;
		}
		
		logger.debug("Looking for API for relative path '{}'", exchange.getRelativePath());
		
		DryApi<TAppContext, TCallContext, TValidator> api = mounts.get(exchange.getRelativePath());
		
		if (api==null) {
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
			exchange.setStatusCode(StatusCodes.NOT_FOUND);
	        exchange.getResponseSender().send("Not found");
	        return;
		}
		
		logger.debug("Reading request");
		
		exchange.getRequestReceiver().receiveFullBytes((ex, data) -> {
			ObjectMapper mapper = JsonUtils.createMapper();
			
			logger.debug("Parsing request");
			
			JsonBatchRequest batch = ExceptionUtils.withRuntimeException(()->mapper.readValue(new String(data, "utf-8"), new TypeReference<JsonBatchRequest>() {}));
			
			logger.info("Executing:\n{}", batch
				.getRequests()
				.stream()
				.map(r->StringUtils.subst("\tuuid={} qualifiedName={} type={}", r.getRequestUuid(), r.getQualifiedName(), r.getRequestType()))
				.collect(Collectors.joining("\n"))
			);
			
			JsonExecutor<TAppContext, TCallContext, TValidator> gate = new JsonExecutor<>(api, validatorProvider);
			
			TAppContext appContext = settings.getAppContextProvider().apply(ex);
			
			try {
				JsonBatchResponse res = gate.execute(appContext, batch);
	
				logger.debug("Execution done:\n{}", res
					.getResponses()
					.stream()
					.map(r->StringUtils.subst("\tuuid={} status={}", r.getRequestUuid(), r.getStatus()))
					.collect(Collectors.joining("\n"))
				);
				
				logger.debug("Sending answer");
				
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.setStatusCode(StatusCodes.OK);
		        exchange.getResponseSender().send(ExceptionUtils.withRuntimeException(()->mapper.writeValueAsString(res)));
		        
		        logger.debug("Done");
			} catch (Throwable t) {
				appContext.reportException(t);
				
				throw t;
			} finally {
				appContext.destroy();
			}
		}, (ex, e) -> {
			logger.error("Exception during request handling", e);
			throw new RuntimeException(e);
		});
	}
}
