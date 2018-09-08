package com.navigo3.dryapi.servlet;

import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class DryApiServlet<TAppContext extends AppContext, TCallContext extends CallContext> extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final JsonExecutor<TAppContext, TCallContext> executor;
	private Function<HttpServletRequest, TAppContext> contextProvider;
	
	public DryApiServlet(DryApi<TAppContext, TCallContext> api, Function<HttpServletRequest, TAppContext> contextProvider) {
		this.executor = new JsonExecutor<>(api);
		this.contextProvider = contextProvider;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		
		ObjectMapper mapper = JsonUtils.createMapper();
		
		JsonBatchRequest batch = ExceptionUtils.withRuntimeException(()->mapper.readValue(body, new TypeReference<JsonBatchRequest>() {}));

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
		
		resp.setCharacterEncoding("UTF-8");
		resp.setStatus(res.getOverallSuccess() ? 200 : 500);
		resp.setContentType("application/json;charset=UTF-8");
		
		resp.getWriter().println(mapper.writeValueAsString(res));
	}
}
