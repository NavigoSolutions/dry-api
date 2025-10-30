package com.navigo3.dryapi.core.util;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;

public class DryApiSslUtils {

	public static X509ExtendedKeyManager buildKeyManager(String httpsKeyCertPath, String httpsCertPath)
		throws Exception {
		Validate.isTrue(httpsKeyCertPath.endsWith(".der"), "Key must be in DER format");
		Validate.isTrue(httpsCertPath.endsWith(".cer"), "Invalid certificate format");

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert;
		try (FileInputStream fis = new FileInputStream(httpsCertPath)) {
			cert = (X509Certificate) cf.generateCertificate(fis);
		}

		byte[] keyBytes = Files.readAllBytes(Paths.get(httpsKeyCertPath));
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = kf.generatePrivate(keySpec);

		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(null, null);
		ks.setKeyEntry("alias", privateKey, "changeit".toCharArray(), new java.security.cert.Certificate[] {
			cert
		});

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, "changeit".toCharArray());
		return (X509ExtendedKeyManager) kmf.getKeyManagers()[0];
	}

	public static X509ExtendedTrustManager buildTrustManager(String httpsCAtPath) throws Exception {
		Validate.isTrue(httpsCAtPath.endsWith(".cer"));

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate caCert;
		try (FileInputStream fis = new FileInputStream(httpsCAtPath)) {
			caCert = (X509Certificate) cf.generateCertificate(fis);
		}

		KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
		ts.load(null, null);
		ts.setCertificateEntry("ca", caCert);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ts);
		return (X509ExtendedTrustManager) tmf.getTrustManagers()[0];
	}
}
