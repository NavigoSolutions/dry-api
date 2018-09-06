package com.navigo3.dryapi.core.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {
	public static ObjectMapper createMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new Jdk8Module());
		objectMapper.registerModule(new JavaTimeModule());
		
		return objectMapper;
	}

	public static void prettyPrint(Object o) {
		ExceptionUtils.withRuntimeException(()->{
			ObjectMapper mapper = JsonUtils.createMapper();
			mapper.setSerializationInclusion(Include.NON_ABSENT);
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
		});
	}
}
