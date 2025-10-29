package com.navigo3.dryapi.test;

import java.util.Set;

import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.sample.impls.TestApi;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;
import com.navigo3.dryapi.server.HttpServer;
import com.navigo3.dryapi.server.ImmutableApiMount;
import com.navigo3.dryapi.server.ImmutableHttpsInterface;
import com.navigo3.dryapi.server.ImmutableHttpsServerSettings;
import com.navigo3.dryapi.test.helpers.RemoteCallsEnvironment;
import com.navigo3.dryapi.test.helpers.RemoteCallsEnvironment.SSlCertPaths;

public class Main {
	private static final int PORT = 8443;

	public static final SSlCertPaths SSL_TEST_CERTS_PATHS = new SSlCertPaths(
		getNavigoCertPath("navigo3.com.cer"),
		getNavigoCertPath("navigo3.com-pkcs8.key"),
		getNavigoCertPath("ca.cer")
	);

	private static String getNavigoCertPath(String filename) {
		Validate.contained(Set.of("navigo3.com.cer", "navigo3.com-pkcs8.key", "ca.cer"), filename);

		var username = System.getProperty("user.name");
		var basepath = "/home/" + username + "/Navigo3/git-production/wildcard-certs/";

		return basepath + filename;
	}

	public static void main(String[] args) throws Exception {
		HttpServer<TestAppContext, TestCallContext, TestValidator> server = new HttpServer<>(
			ImmutableHttpsServerSettings.<TestAppContext, TestCallContext, TestValidator>builder()
				.addHttpsInterfaces(
					ImmutableHttpsInterface.builder()
						.host("localhost")
						.port(PORT)
						.sslContext(new RemoteCallsEnvironment(SSL_TEST_CERTS_PATHS).buildSslContext())
						.build()
				)
				.addApiMounts(
					ImmutableApiMount.<TestAppContext, TestCallContext, TestValidator>builder()
						.basePath("/api")
						.dryApi(TestApi.build())
						.build()
				)
				.appContextProvider(exch -> new TestAppContext(true))
				.build(),
			(appContext, callContext, allowedPaths) -> new TestValidator(allowedPaths)
		);

		server.start();

		server.waitForStopped();
	}
}
