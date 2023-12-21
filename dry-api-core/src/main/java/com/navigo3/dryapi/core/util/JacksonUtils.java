package com.navigo3.dryapi.core.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class JacksonUtils {
	public static ObjectMapper createJsonMapper() {
		return JsonMapper.builder()
			.addModule(new Jdk8Module())
			.addModule(new DateTimeJsonModule())
			.addModule(new DateTimeJsonKeyModule())
			.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
			.build();
	}

	public static String prettyGet(Object o) {
		return ExceptionUtils.withRuntimeException(() -> {
			ObjectMapper objectMapper = JacksonUtils.createJsonMapper();
			objectMapper.setSerializationInclusion(Include.NON_ABSENT);

			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
		});
	}

	public static void prettyPrint(Object o) {
		System.out.println(prettyGet(o));
	}
}
