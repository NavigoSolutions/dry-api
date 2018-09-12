package com.navigo3.dryapi.servlet;

import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.core.util.StringUtils;

public class DryApiServlet<TAppContext extends AppContext, TCallContext extends CallContext> extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(DryApiServlet.class);
    
	private static final long serialVersionUID = 1L;
	
	private final JsonExecutor<TAppContext, TCallContext> executor;
	private Function<HttpServletRequest, TAppContext> contextProvider;
	
	public DryApiServlet(DryApi<TAppContext, TCallContext> api, Function<HttpServletRequest, TAppContext> contextProvider) {
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
			
			JsonBatchRequest batch = ExceptionUtils.withRuntimeException(()->mapper.readValue(body, new TypeReference<JsonBatchRequest>() {}));
	
			logger.info("Executing:\n{}", batch
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
				
				res = appContext.transaction(()->{
					return executor.execute(tmpAppContext, batch);
				});			
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
}
