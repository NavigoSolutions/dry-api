package com.navigo3.dryapi.test.helpers;

import java.nio.file.Paths;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;

import com.navigo3.dryapi.client.ImmutableExtraHeaderParams;
import com.navigo3.dryapi.client.ImmutableRemoteHttpDryApiSettings;
import com.navigo3.dryapi.client.RemoteHttpDryApi;
import com.navigo3.dryapi.sample.impls.TestApi;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;
import com.navigo3.dryapi.server.HttpServer;
import com.navigo3.dryapi.server.ImmutableApiMount;
import com.navigo3.dryapi.server.ImmutableHttpsInterface;
import com.navigo3.dryapi.server.ImmutableHttpsServerSettings;

import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.PemUtils;

public class RemoteCallsEnvironment {
	private static final int PORT = 8777;

	private HttpServer<TestAppContext, TestCallContext, TestValidator> server;
	private RemoteHttpDryApi api;

	public static SSLContext buildSslContext() {
		String httpsKey = "/home/jarek/Navigo3/git-production/wildcard-certs/navigo3.com.key";
		String httpsCert = "/home/jarek/Navigo3/git-production/wildcard-certs/navigo3.com.cer";
		String httpsCA = "/home/jarek/Navigo3/git-production/wildcard-certs/ca.cer";

		X509ExtendedKeyManager keyManager = PemUtils.loadIdentityMaterial(Paths.get(httpsCert), Paths.get(httpsKey));
		X509ExtendedTrustManager trustManager = PemUtils.loadTrustMaterial(Paths.get(httpsCA));

		SSLFactory sslFactory = SSLFactory.builder()
			.withIdentityMaterial(keyManager)
			.withTrustMaterial(trustManager)
			.build();

		return sslFactory.getSslContext();
	}

	public void start() {
		server = new HttpServer<>(
			ImmutableHttpsServerSettings.<TestAppContext, TestCallContext, TestValidator>builder()
				.addHttpsInterfaces(
					ImmutableHttpsInterface.builder().host("localhost").port(PORT).sslContext(buildSslContext()).build()
				)
				.addApiMounts(
					ImmutableApiMount.<TestAppContext, TestCallContext, TestValidator>builder()
						.basePath("/test/xxx")
						.dryApi(TestApi.build())
						.build()
				)
				.appContextProvider(exch -> new TestAppContext(true))
				.build(),
			(appContext, callContext, allowedPaths) -> new TestValidator(allowedPaths)
		);

		server.start();

		api = new RemoteHttpDryApi(
			"https://localhost:" + PORT + "/test/xxx",
			ImmutableRemoteHttpDryApiSettings.builder().build()
		);

		api.start(httpClient -> ImmutableExtraHeaderParams.builder().build());
	}

	public void stop() {
		server.stop();

		api.stop((httpClient, extraParams) -> {
		});
	}

	public RemoteHttpDryApi getApi() {
		return api;
	}
}
