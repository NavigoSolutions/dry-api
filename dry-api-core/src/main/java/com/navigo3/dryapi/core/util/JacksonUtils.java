package com.navigo3.dryapi.core.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class JacksonUtils {

	public static ObjectMapper createJsonMapper() {
		return createJsonMapper(DryApiConstants.DEFAULT_MAX_SERIALIZABLE_STRING_LENGTH);
	}

	public static ObjectMapper createJsonMapper(int maxSerializableStringLength) {
		var mapper = JsonMapper.builder()
			.addModule(new Jdk8Module())
			.addModule(new DateTimeJsonModule())
			.addModule(new DateTimeJsonKeyModule())
			.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
			.build();

		var constraints = StreamReadConstraints.builder().maxStringLength(maxSerializableStringLength).build();
		mapper.getFactory().setStreamReadConstraints(constraints);

		return mapper;
	}

	public static String prettyGet(Object o, int maxSerializableStringLength) {
		return ExceptionUtils.withRuntimeException(() -> {
			ObjectMapper objectMapper = JacksonUtils.createJsonMapper(maxSerializableStringLength);
			objectMapper.setSerializationInclusion(Include.NON_ABSENT);

			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
		});
	}

	public static void prettyPrint(Object o, int maxSerializableStringLength) {
		System.out.println(prettyGet(o, maxSerializableStringLength));
	}
}
