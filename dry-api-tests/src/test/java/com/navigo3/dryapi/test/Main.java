package com.navigo3.dryapi.test;

import com.navigo3.dryapi.sample.impls.TestApi;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;
import com.navigo3.dryapi.server.HttpServer;
import com.navigo3.dryapi.server.ImmutableApiMount;
import com.navigo3.dryapi.server.ImmutableHttpsInterface;
import com.navigo3.dryapi.server.ImmutableHttpsServerSettings;

public class Main {
	private static final int PORT = 8443;

	public static void main(String[] args) {
		HttpServer<TestAppContext, TestCallContext, TestValidator> server = new HttpServer<>(ImmutableHttpsServerSettings
			.<TestAppContext, TestCallContext, TestValidator>builder()
			.addHttpsInterfaces(ImmutableHttpsInterface.builder().host("localhost").port(PORT).build())
			.addApiMounts(ImmutableApiMount
				.<TestAppContext, TestCallContext, TestValidator>builder()
				.basePath("/api")
				.dryApi(TestApi.build())
				.build()
			)
			.appContextProvider(exch->new TestAppContext(true))
			.build(),
			(appContext, callContext, allowedPaths)->new TestValidator(allowedPaths)
		);
		
		server.start();
		
		server.waitForStopped();
	}	
}
