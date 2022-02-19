package com.navigo3.dryapi.server;

import java.io.IOException;
import java.time.Duration;
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
import com.navigo3.dryapi.core.util.DryApiConstants;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.Function3;
import com.navigo3.dryapi.core.util.JacksonUtils;
import com.navigo3.dryapi.core.util.LambdaUtils.ConsumerWithException;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.ThreadUtils;
import com.navigo3.dryapi.core.validation.Validator;
import com.navigo3.dryapi.server.HttpsServerSettings.ApiMount;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

public class HttpServer<TAppContext extends AppContext, TCallContext extends CallContext, TValidator extends Validator> {
	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

	private final Undertow server;

	private final Map<String, DryApi<TAppContext, TCallContext, TValidator>> mounts;

	private HttpsServerSettings<TAppContext, TCallContext, TValidator> settings;

	private Function3<TAppContext, TCallContext, ObjectPathsTree, TValidator> validatorProvider;

	public HttpServer(HttpsServerSettings<TAppContext, TCallContext, TValidator> settings,
		Function3<TAppContext, TCallContext, ObjectPathsTree, TValidator> validatorProvider) {
		this.settings = settings;
		this.validatorProvider = validatorProvider;

		Builder builder = Undertow.builder();

		settings.getHttpsInterfaces()
			.forEach(i -> builder.addHttpsListener(i.getPort(), i.getHost(), i.getSslContext()));

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
		waitForStopped();
	}

	public void waitForStopped() {
		while (server != null && server.getWorker() != null && !server.getWorker().isShutdown()) {
			ThreadUtils.optimisticSleep(Duration.ofMillis(100));
		}
	}

	private void handleRequest(HttpServerExchange exchange) throws Exception {
		logger.debug("Handling {} request {}", exchange.getRequestMethod().toString(), exchange.getRelativePath());

		if (settings.getExtraUriHandlers().containsKey(exchange.getRelativePath())) {
			logger.debug("Using extra handler");

			settings.getExtraUriHandlers().get(exchange.getRelativePath()).accept(exchange);
			return;
		}

		String origin = exchange.getRequestHeaders().getFirst("Origin");

		if (exchange.getRequestMethod().equalToString("OPTIONS")) {
			exchange.setStatusCode(200);

			logger.debug("Handling OPTION request for origin {}", origin);

			if (settings.getAllowedOrigins().isEmpty() || settings.getAllowedOrigins().contains(origin)) {
				exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), origin);
				exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "*");
				exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), "*");

				exchange.getResponseSender().send("OK");
			} else {
				exchange.getResponseSender()
					.send("This origin is not allowed, please add supported origins into HttpServerSettings of DryApi");
			}

			return;
		}

		exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), origin);

		safelyHandleRequest(exchange, appContext -> {
			HeaderValues contentTypeHeaders = exchange.getRequestHeaders().get(Headers.CONTENT_TYPE);
			String rawContentType = contentTypeHeaders != null ? contentTypeHeaders.getFirst() : "";

			String contentType = StringUtils
				.defaultString(rawContentType)
				.trim()
				.replaceAll("\\s", "")
				.toLowerCase();

			String outputContentType;

			if (DryApiConstants.JSON_MIME.equals(contentType)) {
				outputContentType = DryApiConstants.JSON_MIME;
			} else {
				logger.info("Content type '{}' not supported", rawContentType);

				exchange.setStatusCode(415);
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(StringUtils.subst("Please use content type: '{}' instead of '{}'",
					DryApiConstants.JSON_MIME, contentType));

				return;
			}

			logger.debug("Looking for API for relative path '{}'", exchange.getRelativePath());

			DryApi<TAppContext, TCallContext, TValidator> api = mounts.get(exchange.getRelativePath());

			if (api == null) {
				if (settings.getNotFoundUriHandler().isPresent()) {
					logger.debug("Using not found handler");

					settings.getNotFoundUriHandler().get().accept(exchange);
					return;
				} else {
					exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
					exchange.setStatusCode(StatusCodes.NOT_FOUND);
					exchange.getResponseSender().send("API not found");
					return;
				}
			}

			logger.debug("Reading request");

			exchange.getRequestReceiver().receiveFullBytes((ex, data) -> {

				String content = ExceptionUtils.withRuntimeException(() -> new String(data, "utf-8"));

				ObjectMapper objectMapper = JacksonUtils.createJsonMapper();

				logger.debug("Parsing request");

				JsonBatchRequest batch = ExceptionUtils
					.withRuntimeException(() -> objectMapper.readValue(content, new TypeReference<JsonBatchRequest>() {
					}));

				logger.info("Executing:\n{}", batch
					.getRequests()
					.stream()
					.map(r -> StringUtils.subst("\tuuid={} qualifiedName={} type={}", r.getRequestUuid(),
						r.getQualifiedName(), r.getRequestType()))
					.collect(Collectors.joining("\n")));

				JsonExecutor<TAppContext, TCallContext, TValidator> gate = new JsonExecutor<>(api, validatorProvider,
					(qualifiedName, duration, context) -> {
					});

				JsonBatchResponse res = gate.execute(appContext, batch, objectMapper);

				logger.debug("Execution done:\n{}", res
					.getResponses()
					.stream()
					.map(r -> StringUtils.subst("\tuuid={} status={}", r.getRequestUuid(), r.getStatus()))
					.collect(Collectors.joining("\n")));

				logger.debug("Sending answer");

				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, outputContentType);
				exchange.setStatusCode(res.getOverallSuccess() ? StatusCodes.OK : StatusCodes.BAD_REQUEST);
				exchange.getResponseSender()
					.send(ExceptionUtils.withRuntimeException(() -> objectMapper.writeValueAsString(res)));

				logger.debug("Done");

			}, (ex, e) -> {
				logger.error("Exception during request handling", e);
				throw new RuntimeException(e);
			});
		});
	}

	private void safelyHandleRequest(HttpServerExchange exchange, ConsumerWithException<TAppContext> block)
		throws IOException {
		TAppContext appContext = null;

		try {
			logger.debug("Getting context");
			appContext = settings.getAppContextProvider().apply(exchange);

			if (!appContext.getIsAuthenticated()) {
				logger.info("Not authorized");
				exchange.setStatusCode(401);
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send("Not authorized");
				return;
			}

			block.accept(appContext);
		} catch (Throwable t) {
			logger.error("Exception during request handling", t);

			if (appContext != null) {
				appContext.reportException(t);
			}

			exchange.setStatusCode(500);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");

			if (appContext != null && appContext.getIsDevelopmentInstance()) {
				exchange.getResponseSender().send(ExceptionUtils.extractStacktrace("Internal error", t));
			} else {
				exchange.getResponseSender().send("Internal error");
			}
		} finally {
			if (appContext != null) {
				appContext.destroy();
			}
		}
	}
}
