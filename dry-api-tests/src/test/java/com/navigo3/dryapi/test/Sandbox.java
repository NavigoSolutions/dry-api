package com.navigo3.dryapi.test;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.exec.json.JsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchResponse;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.util.JsonUtils;

public class Sandbox {

	@Test
	public void test() {
		JsonUtils.prettyPrint(TypeSchema.build(new TypeReference<JsonBatchRequest>() {}).getDefinitions());
		JsonUtils.prettyPrint(TypeSchema.build(new TypeReference<JsonBatchResponse>() {}).getDefinitions());
	}
	
}
