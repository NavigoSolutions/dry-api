package com.navigo3.dryapi.client;

import java.util.Map;

import org.immutables.value.Value;

@Value.Immutable
public interface ExtraHeaderParams {
	Map<String, String> getHeaders();
	Map<String, String> getCookies();
}