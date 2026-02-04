package com.navigo3.dryapi.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.exec.json.ImmutableJsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.ImmutableJsonRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchResponse;
import com.navigo3.dryapi.core.exec.json.JsonExecutor;
import com.navigo3.dryapi.core.exec.json.JsonRequest.RequestType;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.util.Consumer3;
import com.navigo3.dryapi.core.util.DryApiConstants;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.Function3;
import com.navigo3.dryapi.core.util.JacksonUtils;
import com.navigo3.dryapi.core.util.LambdaUtils.ConsumerWithException;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.utils.DownloadParamInterface;
import com.navigo3.dryapi.core.validation.Validator;
import com.navigo3.dryapi.predefined.params.DownloadParam;

public class DryApiServlet<TAppContext extends AppContext, TCallContext extends CallContext, TValidator extends Validator> extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(DryApiServlet.class);

	private static final long serialVersionUID = 1L;

	private DryApi<TAppContext, TCallContext, TValidator> api;
	private final JsonExecutor<TAppContext, TCallContext, TValidator> executor;
	private Function<HttpServletRequest, TAppContext> contextProvider;

	public DryApiServlet(DryApi<TAppContext, TCallContext, TValidator> api,
		Function<HttpServletRequest, TAppContext> contextProvider,
		Function3<TAppContext, TCallContext, ObjectPathsTree, TValidator> validatorProvider,
		Consumer3<String, Duration, TAppContext> statsConsumer) {
		this.api = api;
		this.executor = new JsonExecutor<>(api, validatorProvider, statsConsumer);
		this.contextProvider = contextProvider;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("Handling POST request");

		safelyHandleRequest(req, resp, appContext -> {
			String contentType = StringUtils.defaultString(req.getContentType())
				.trim()
				.replaceAll("\\s", "")
				.toLowerCase();

			if (DryApiConstants.JSON_MIME.equals(contentType)) {
				//
			} else {
				logger.info("Content type '{}' not supported", req.getContentType());

				resp.setCharacterEncoding("UTF-8");
				resp.setStatus(415);
				resp.setContentType("text/plain");
				resp.getOutputStream()
					.println(StringUtils.subst("Please use content type: '{}'", DryApiConstants.JSON_MIME));

				return;
			}

			logger.debug("Reading request");

			String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

			ObjectMapper objectMapper = JacksonUtils.createJsonMapper(api.getConfig().getMaxSerializableStringLength());

			logger.debug("Parsing request");

			JsonBatchRequest batchRequest = ExceptionUtils.withRuntimeException(
				() -> objectMapper.readValue(body, new TypeReference<JsonBatchRequest>() {
				})
			);

			appContext.start(
				batchRequest.getRequests().stream().map(r -> r.getQualifiedName()).collect(Collectors.toList())
			);

			JsonBatchResponse res = executeBatch(batchRequest, req, appContext, objectMapper);

			logger.debug("Sending answer");

			resp.setCharacterEncoding("UTF-8");
			resp.setStatus(res.getOverallSuccess() ? 200 : 400);
			resp.setContentType("application/json;charset=utf-8");

			objectMapper.writeValue(resp.getWriter(), res);

			logger.debug("Done");
		});
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("Handling GET request");

		safelyHandleRequest(req, resp, appContext -> {
			ObjectMapper objectMapper = JacksonUtils.createJsonMapper(api.getConfig().getMaxSerializableStringLength());

			logger.debug("Extracting params");

			String qualifiedName = req.getParameter("qualifiedName");
			JsonNode input = objectMapper.readTree(req.getParameter("input"));
			boolean forceDownload = Boolean.valueOf(req.getParameter("forceDownload"));

			logger.debug("Checking that method returns DownloadParam");

			Optional<MethodDefinition> def = api.lookupDefinition(qualifiedName);

			Validate.isPresent(def);

			Validate.isTrue(
				DownloadParamInterface.class.isAssignableFrom(((Class<?>) def.get().getOutputType().getType())),
				"Only method returning dry-api type DownloadParam is allowed for GET access!"
			);

			logger.debug("Building request");

			JsonBatchRequest batchRequest = ImmutableJsonBatchRequest.builder()
				.addRequests(
					ImmutableJsonRequest.builder()
						.qualifiedName(qualifiedName)
						.input(input)
						.requestType(RequestType.EXECUTE)
						.requestUuid(UUID.randomUUID().toString())
						.build()
				)
				.build();

			appContext.start(
				batchRequest.getRequests().stream().map(r -> r.getQualifiedName()).collect(Collectors.toList())
			);

			logger.debug("Sending answer");

			JsonBatchResponse res = executeBatch(batchRequest, req, appContext, objectMapper);

			if (res.getOverallSuccess()) {
				DownloadParamInterface downloadData = objectMapper.convertValue(
					res.getResponses().get(0).getOutput().get(),
					new TypeReference<DownloadParam>() {
					}
				);

				byte[] data = Base64.getDecoder().decode(downloadData.getContentBase64());

				String encodedName = URLEncoder.encode(downloadData.getName(), StandardCharsets.UTF_8.toString())
					.replace("+", "%20");

				resp.setStatus(200);
				resp.setContentType(downloadData.getMimeType());
				resp.setHeader(
					"Content-Disposition",
					StringUtils.subst(
						"{}; filename=\"{}\"; filename*=utf-8''{}",
						forceDownload == false ? "inline" : "attachment",
						StringUtils.toAscii(downloadData.getName()),
						encodedName
					)
				);
				resp.setContentLength(data.length);

				resp.getOutputStream().write(data);
			} else {
				resp.setStatus(500);
				resp.setContentType("text/plain");

				logger.error("Error message: {}", res.getResponses().get(0).getErrorMessage().orElse("?"));
				logger.error("Response: {}", res);

				appContext.reportException(
					new RuntimeException(res.getResponses().get(0).getErrorMessage().orElse("?"))
				);

				resp.getOutputStream().write("Cannot download file. We are sorry.".getBytes());
			}
		});
	}

	private void safelyHandleRequest(HttpServletRequest req, HttpServletResponse resp,
		ConsumerWithException<TAppContext> block) throws IOException {
		TAppContext appContext = null;

		try {
			logger.debug("Getting context");
			appContext = contextProvider.apply(req);

			if (!appContext.getIsAuthenticated()) {
				logger.info("Not authorized");
				resp.setStatus(401);
				resp.setContentType("text/plain");
				resp.getWriter().println("Not authorized");
				return;
			}

			block.accept(appContext);
		} catch (Throwable t) {
			logger.error("Exception during request handling", t);

			if (appContext != null) {
				appContext.reportException(t);
			}

			resp.setStatus(500);
			resp.setContentType("text/plain");

			if (appContext != null && appContext.getIsDevelopmentInstance()) {
				resp.getWriter().println(ExceptionUtils.extractStacktrace("Internal error", t));
			} else {
				resp.getWriter().println("Internal error");
			}
		} finally {
			if (appContext != null) {
				appContext.destroy();
			}
		}
	}

	private JsonBatchResponse executeBatch(JsonBatchRequest batchRequest, HttpServletRequest req,
		TAppContext appContext, ObjectMapper objectMapper) {
		logger.info(
			"Executing:\n{}",
			batchRequest.getRequests()
				.stream()
				.map(
					r -> StringUtils.subst(
						"\tuuid={} qualifiedName={} type={}",
						r.getRequestUuid(),
						r.getQualifiedName(),
						r.getRequestType()
					)
				)
				.collect(Collectors.joining("\n"))
		);

		JsonBatchResponse res = null;

		try {
			res = executor.execute(appContext, batchRequest, objectMapper);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

		logger.debug(
			"Execution done:\n{}",
			res.getResponses()
				.stream()
				.map(r -> StringUtils.subst("\tuuid={} status={}", r.getRequestUuid(), r.getStatus()))
				.collect(Collectors.joining("\n"))
		);

		return res;
	}
}
