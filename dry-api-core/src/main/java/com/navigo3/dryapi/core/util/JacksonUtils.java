package com.navigo3.dryapi.core.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class JacksonUtils {

	public enum DataFormat {
		JSON,
		XML
	}
	
	public static ObjectMapper createMapper(DataFormat format) {
		switch (format) {
			case JSON: return createJsonMapper();
			case XML: return createXmlMapper();
			default: throw new RuntimeException("Unexpected type "+format);
		}
	}
	
	public static ObjectMapper createJsonMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new Jdk8Module());
		objectMapper.registerModule(new DateTimeJsonModule());
		objectMapper.registerModule(new DateTimeJsonKeyModule());
		objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
		
		return objectMapper;
	}
	
	public static ObjectMapper createXmlMapper() {
		XmlMapper objectMapper = new XmlMapper();
		objectMapper.registerModule(new Jdk8Module());
		objectMapper.registerModule(new DateTimeJsonModule());
		objectMapper.registerModule(new DateTimeXmlKeyModule());
		objectMapper.setSerializationInclusion(Include.ALWAYS);
		objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
		
		return objectMapper;
	}

	public static String prettyGet(Object o) {
		return ExceptionUtils.withRuntimeException(()->{
			ObjectMapper objectMapper = JacksonUtils.createJsonMapper();
			objectMapper.setSerializationInclusion(Include.NON_ABSENT);

			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
		});
	}

	public static void prettyPrint(Object o) {
		System.out.println(prettyGet(o));
	}

	public static String getMimeForFormat(DataFormat format) {
		switch (format) {
			case JSON: return DryApiConstants.JSON_MIME;
			case XML: return DryApiConstants.XML_MIME;
			default: throw new RuntimeException("Unexpected type "+format);
		}
	}
}
