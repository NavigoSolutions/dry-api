package com.navigo3.dryapi.test.helpers;

import com.navigo3.dryapi.client.ImmutableExtraHeaderParams;
import com.navigo3.dryapi.client.ImmutableRemoteHttpDryApiSettings;
import com.navigo3.dryapi.client.RemoteHttpDryApi;
import com.navigo3.dryapi.sample.impls.TestApi;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;
import com.navigo3.dryapi.server.HttpServer;
import com.navigo3.dryapi.server.ImmutableApiMount;
import com.navigo3.dryapi.server.ImmutableHttpInterface;
import com.navigo3.dryapi.server.ImmutableHttpServerSettings;

public class RemoteCallsEnvironment {
	private static final int PORT = 8777;
	
	private HttpServer<TestAppContext, TestCallContext, TestValidator> server;
	private RemoteHttpDryApi api;
	
	public void start() {
		server = new HttpServer<>(ImmutableHttpServerSettings
			.<TestAppContext, TestCallContext, TestValidator>builder()
			.addHttpInterfaces(ImmutableHttpInterface.builder().host("localhost").port(PORT).build())
			.addApiMounts(ImmutableApiMount.<TestAppContext, TestCallContext, TestValidator>builder().basePath("/test/xxx").dryApi(TestApi.build()).build())
			.appContextProvider(exch->new TestAppContext(true))
			.build(),
			(appContext, callContext, allowedPaths)->new TestValidator(allowedPaths)
		);
		
		server.start();
		
		api = new RemoteHttpDryApi("http://localhost:"+PORT+"/test/xxx", ImmutableRemoteHttpDryApiSettings
			.builder()
			.build()
		);
		
		api.start(httpClient->ImmutableExtraHeaderParams.builder().build());
	}

	public void stop() {
		server.stop();
		
		api.stop((httpClient, extraParams)->{});
	}

	public RemoteHttpDryApi getApi() {
		return api;
	}
}
