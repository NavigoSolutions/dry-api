package com.navigo3.dryapi.test.helpers;

import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.navigo3.dryapi.client.ImmutableExtraHeaderParams;
import com.navigo3.dryapi.client.ImmutableRemoteHttpDryApiSettings;
import com.navigo3.dryapi.client.RemoteHttpDryApi;
import com.navigo3.dryapi.core.context.DryApiSslUtils;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.sample.impls.TestApi;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;
import com.navigo3.dryapi.server.HttpServer;
import com.navigo3.dryapi.server.ImmutableApiMount;
import com.navigo3.dryapi.server.ImmutableHttpsInterface;
import com.navigo3.dryapi.server.ImmutableHttpsServerSettings;

public class RemoteCallsEnvironment {
	private static final int PORT = 8777;

	private HttpServer<TestAppContext, TestCallContext, TestValidator> server;
	private RemoteHttpDryApi api;

	public static SSLContext buildSslContext() throws Exception {

		String keyPath = getNavigoCertPath("navigo3.com.key");
		String certPath = getNavigoCertPath("navigo3.com.cer");

		var keyManager = DryApiSslUtils.buildKeyManager(keyPath, certPath);
		var trustManager = DryApiSslUtils.buildTrustManager(certPath);

		var sslContext = SSLContext.getInstance("TLS");
		sslContext.init(new KeyManager[] {
			keyManager
		}, new TrustManager[] {
			trustManager
		}, null);

		return sslContext;
	}

	public static String getNavigoCertPath(String filename) {
		Validate.isTrue(filename.endsWith(".cer") || filename.endsWith(".key"));
		Validate.contained(Set.of("navigo3.com.cer", "navigo3.com.key", "ca.cer"), filename);

		var username = System.getProperty("user.name");
		var basepath = "/home/" + username + "/Navigo3/git-production/wildcard-certs/";

		return basepath + filename;
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
			DryApiSslUtils.buildTrustManager(getNavigoCertPath("ca.cer"))
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
