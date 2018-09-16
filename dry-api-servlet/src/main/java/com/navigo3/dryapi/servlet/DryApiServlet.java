package com.navigo3.dryapi.servlet;

import java.io.IOException;
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
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.predefined.params.DownloadParam;

public class DryApiServlet<TAppContext extends AppContext, TCallContext extends CallContext> extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(DryApiServlet.class);
    
	private static final long serialVersionUID = 1L;

	private DryApi<TAppContext, TCallContext> api;
	private final JsonExecutor<TAppContext, TCallContext> executor;
	private Function<HttpServletRequest, TAppContext> contextProvider;
	
	public DryApiServlet(DryApi<TAppContext, TCallContext> api, Function<HttpServletRequest, TAppContext> contextProvider) {
		this.api = api;
		this.executor = new JsonExecutor<>(api);
		this.contextProvider = contextProvider;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("Handling request");
		
		try {
			logger.debug("Reading request");
			
			String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			
			ObjectMapper mapper = JsonUtils.createMapper();
			
			logger.debug("Parsing request");
			
			JsonBatchRequest batchRequest = ExceptionUtils.withRuntimeException(()->mapper.readValue(body, new TypeReference<JsonBatchRequest>() {}));
			
			JsonBatchResponse res = executeBatch(batchRequest, req);
			
			logger.debug("Sending answer");
			
			resp.setCharacterEncoding("UTF-8");
			resp.setStatus(res.getOverallSuccess() ? 200 : 500);
			resp.setContentType("application/json;charset=UTF-8");
			
			resp.getWriter().println(mapper.writeValueAsString(res));
			
			logger.debug("Done");
		} catch (Throwable t) {
			logger.error("Exception during request handling", t);
			
			throw t;
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			ObjectMapper mapper = JsonUtils.createMapper();
			
			logger.debug("Extracting params");
			
			String qualifiedName = req.getParameter("qualifiedName");
			JsonNode input = mapper.readTree(req.getParameter("input"));
			boolean forceDownload = Boolean.valueOf(req.getParameter("forceDownload"));
	
			logger.debug("Checking that method returns DownloadParam");
			
			Optional<MethodDefinition> def = api.lookupDefinition(qualifiedName);
			
			Validate.isPresent(def);
			Validate.equals(def.get().getOutputType().getType().getTypeName(), "com.navigo3.dryapi.predefined.params.DownloadParam", 
				"Only method returning dry-api type DownloadParam is allowed for GET access!");
			
			logger.debug("Building request");
			
			JsonBatchRequest batchRequest = ImmutableJsonBatchRequest
				.builder()
				.addRequests(ImmutableJsonRequest
					.builder()
					.qualifiedName(qualifiedName)
					.input(input)
					.requestType(RequestType.EXECUTE)
					.requestUuid(UUID.randomUUID().toString())
					.build()
				)
				.build();
			
			logger.debug("Sending answer");
			
			JsonBatchResponse res = executeBatch(batchRequest, req);
			
			Validate.isTrue(res.getOverallSuccess());
			
			DownloadParam downloadData = mapper.convertValue(res.getResponses().get(0).getOutput().get(), new TypeReference<DownloadParam>(){});
			
			byte[] data = Base64.getDecoder().decode(downloadData.getContentBase64());
			
			resp.setStatus(200);
			resp.setContentType(downloadData.getName());
			resp.setHeader("Content-Disposition", StringUtils.subst("{}; filename=\"{}\"", forceDownload==false ? "inline" : "attachment", downloadData.getName()));
			resp.setContentLength(data.length);
			
			resp.getOutputStream().write(data);		
		} catch (Throwable t) {
			logger.error("Exception during request handling", t);
			
			throw t;
		}
	}
	
	private JsonBatchResponse executeBatch(JsonBatchRequest batchRequest, HttpServletRequest req) {
		logger.info("Executing:\n{}", batchRequest
			.getRequests()
			.stream()
			.map(r->StringUtils.subst("\tuuid={} qualifiedName={} type={}", r.getRequestUuid(), r.getQualifiedName(), r.getRequestType()))
			.collect(Collectors.joining("\n"))
		);
		
		TAppContext appContext = null;
		
		JsonBatchResponse res = null;
		
		try {
			appContext = contextProvider.apply(req);
			
			TAppContext tmpAppContext = appContext;
			
			res = executor.execute(tmpAppContext, batchRequest);	
		} finally {
			if (appContext!=null) {
				appContext.destroy();
			}
		}
		
		logger.debug("Execution done:\n{}", res
			.getResponses()
			.stream()
			.map(r->StringUtils.subst("\tuuid={} status={}", r.getRequestUuid(), r.getStatus()))
			.collect(Collectors.joining("\n"))
		);
		
		return res;
	}
}
