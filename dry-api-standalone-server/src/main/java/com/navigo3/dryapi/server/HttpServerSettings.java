package com.navigo3.dryapi.server;

import java.util.List;
import java.util.function.Function;

import org.immutables.value.Value;
import org.immutables.value.Value.Check;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.util.Validate;

import io.undertow.server.HttpServerExchange;

@Value.Immutable
public interface HttpServerSettings<TAppContext extends AppContext, TCallContext extends CallContext> {
	@Value.Immutable
	public interface HttpInterface {
		String getHost();
		int getPort();
		
		@Check
		default void check() {
			Validate.greaterThanZero(getPort());
		}
	}
	
	@Value.Immutable
	public interface ApiMount<TAppContext extends AppContext, TCallContext extends CallContext> {
		String getBasePath();
		DryApi<TAppContext, TCallContext> getDryApi();
		
		@Check
		default void check() {
			Validate.passRegex(getBasePath(), "/"+DryApi.PATH_PATTERN);
		}
	}
	
	List<HttpInterface> getHttpInterfaces();
	
	List<ApiMount<TAppContext, TCallContext>> getApiMounts();
	
	Function<HttpServerExchange, TAppContext> getContextProvider();
	
	@Check
	default void check() {
		Validate.notEmpty(getHttpInterfaces());
		Validate.notEmpty(getApiMounts());
		Validate.hasUniqueProperty(getHttpInterfaces(), HttpInterface::getPort);
		Validate.hasUniqueProperty(getApiMounts(), ApiMount::getBasePath);
	}
}
