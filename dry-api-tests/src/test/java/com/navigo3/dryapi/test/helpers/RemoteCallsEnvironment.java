package com.navigo3.dryapi.test.helpers;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.navigo3.dryapi.client.ImmutableExtraHeaderParams;
import com.navigo3.dryapi.client.ImmutableRemoteHttpDryApiSettings;
import com.navigo3.dryapi.client.RemoteHttpDryApi;
import com.navigo3.dryapi.core.context.DryApiSslUtils;
import com.navigo3.dryapi.sample.impls.TestApi;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;
import com.navigo3.dryapi.server.HttpServer;
import com.navigo3.dryapi.server.ImmutableApiMount;
import com.navigo3.dryapi.server.ImmutableHttpsInterface;
import com.navigo3.dryapi.server.ImmutableHttpsServerSettings;

public class RemoteCallsEnvironment {

	public record SSlCertPaths(String httpsCertPath, String pkcs8KeyPath, String httpsCAPath) {

	}

	private static final int PORT = 8777;

	private HttpServer<TestAppContext, TestCallContext, TestValidator> server;
	private RemoteHttpDryApi api;

	private final SSlCertPaths certPaths;

	public RemoteCallsEnvironment(SSlCertPaths certPaths) {
		this.certPaths = certPaths;

	}

	public SSLContext buildSslContext() throws Exception {

		var keyManager = DryApiSslUtils.buildKeyManager(certPaths.pkcs8KeyPath(), certPaths.httpsCertPath());
		var trustManager = DryApiSslUtils.buildTrustManager(certPaths.httpsCAPath());

		var sslContext = SSLContext.getInstance("TLS");
		sslContext.init(new KeyManager[] {
			keyManager
		}, new TrustManager[] {
			trustManager
		}, null);

		return sslContext;
	}

	public void start() throws Exception {

		var sslContext = buildSslContext();

		server = new HttpServer<>(
			ImmutableHttpsServerSettings.<TestAppContext, TestCallContext, TestValidator>builder()
				.addHttpsInterfaces(
					ImmutableHttpsInterface.builder()
						.host("localhost.navigo3.com")
						.port(PORT)
						.sslContext(sslContext)
						.build()
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
			"https://localhost.navigo3.com:" + PORT + "/test/xxx",
			ImmutableRemoteHttpDryApiSettings.builder().build(),
			sslContext,
			DryApiSslUtils.buildTrustManager(certPaths.httpsCAPath())
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
