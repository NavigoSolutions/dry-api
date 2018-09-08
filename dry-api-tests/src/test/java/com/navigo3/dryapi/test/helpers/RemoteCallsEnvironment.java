package com.navigo3.dryapi.test.helpers;

import com.navigo3.dryapi.client.RemoteHttpDryApi;
import com.navigo3.dryapi.client.RemoteHttpDryApiSettings;
import com.navigo3.dryapi.sample.impls.TestApi;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.server.HttpServer;
import com.navigo3.dryapi.server.ImmutableApiMount;
import com.navigo3.dryapi.server.ImmutableHttpInterface;
import com.navigo3.dryapi.server.ImmutableHttpServerSettings;

public class RemoteCallsEnvironment {
	private HttpServer<TestAppContext, TestCallContext> server;
	private RemoteHttpDryApi api;
	
	public void start() {
		server = new HttpServer<>(ImmutableHttpServerSettings
			.<TestAppContext, TestCallContext>builder()
			.addHttpInterfaces(ImmutableHttpInterface.builder().host("localhost").port(8080).build())
			.addApiMounts(ImmutableApiMount.<TestAppContext, TestCallContext>builder().basePath("/test/xxx").dryApi(TestApi.build()).build())
			.appContextProvider(exch->new TestAppContext(true))
			.build()
		);
		
		server.start();
		
		api = new RemoteHttpDryApi("http://localhost:8080/test/xxx", RemoteHttpDryApiSettings.buildDefault());
		api.start();
	}

	public void stop() {
		server.stop();
		
		api.stop();
	}

	public RemoteHttpDryApi getApi() {
		return api;
	}
}
