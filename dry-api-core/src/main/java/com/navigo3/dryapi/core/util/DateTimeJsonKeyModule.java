package com.navigo3.dryapi.core.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class DateTimeJsonKeyModule extends SimpleModule {
	private static final long serialVersionUID = 1L;

	public DateTimeJsonKeyModule() {
		addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
			@Override
			public void serialize(LocalDate date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException, JsonProcessingException {
				jsonGenerator.writeString(DryApiConstants.DATE_FORMATER.format(date));
			}
		});

		addSerializer(LocalTime.class, new JsonSerializer<LocalTime>() {
			@Override
			public void serialize(LocalTime date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException, JsonProcessingException {
				jsonGenerator.writeString(DryApiConstants.TIME_FORMATER.format(date));
			}
		});

		addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
			@Override
			public void serialize(LocalDateTime date, JsonGenerator jsonGenerator,
				SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
				jsonGenerator.writeString(DryApiConstants.DATETIME_FORMATER.format(date));
			}
		});

		addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {

			@Override
			public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {

				if (p.getText().contains(" ")) {
					return LocalDateTime.parse(p.getText(), DryApiConstants.DATETIME_FORMATER).toLocalDate();
				} else {
					return LocalDate.parse(p.getText(), DryApiConstants.DATE_FORMATER);
				}
			}
		});

		addDeserializer(LocalTime.class, new JsonDeserializer<LocalTime>() {

			@Override
			public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {

				return LocalTime.parse(p.getText(), DryApiConstants.TIME_FORMATER);
			}
		});

		addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {

			@Override
			public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {

				if (p.getText().contains(" ")) {
					return LocalDateTime.parse(p.getText(), DryApiConstants.DATETIME_FORMATER);
				} else {
					return LocalDate.parse(p.getText(), DryApiConstants.DATE_FORMATER).atStartOfDay();
				}
			}
		});

		// -------------------------------------------------------------------------------------------------------------

		addKeySerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
			@Override
			public void serialize(LocalDate date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException, JsonProcessingException {
				jsonGenerator.writeFieldName(DryApiConstants.DATE_FORMATER.format(date));
			}
		});

		addKeySerializer(LocalTime.class, new JsonSerializer<LocalTime>() {
			@Override
			public void serialize(LocalTime date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException, JsonProcessingException {
				jsonGenerator.writeFieldName(DryApiConstants.TIME_FORMATER.format(date));
			}
		});

		addKeySerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
			@Override
			public void serialize(LocalDateTime date, JsonGenerator jsonGenerator,
				SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
				jsonGenerator.writeFieldName(DryApiConstants.DATETIME_FORMATER.format(date));
			}
		});

		addKeyDeserializer(LocalDate.class, new KeyDeserializer() {

			@Override
			public LocalDate deserializeKey(String key, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {

				if (StringUtils.isBlank(key)) {
					return null;
				}

				if (key.contains(" ")) {
					return LocalDateTime.parse(key, DryApiConstants.DATETIME_FORMATER).toLocalDate();
				} else {
					return LocalDate.parse(key, DryApiConstants.DATE_FORMATER);
				}
			}
		});

		addKeyDeserializer(LocalTime.class, new KeyDeserializer() {

			@Override
			public LocalTime deserializeKey(String key, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {

				if (StringUtils.isBlank(key)) {
					return null;
				}

				return LocalTime.parse(key, DryApiConstants.TIME_FORMATER);
			}
		});

		addKeyDeserializer(LocalDateTime.class, new KeyDeserializer() {

			@Override
			public LocalDateTime deserializeKey(String key, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {

				if (StringUtils.isBlank(key)) {
					return null;
				}

				if (key.contains(" ")) {
					return LocalDateTime.parse(key, DryApiConstants.DATETIME_FORMATER);
				} else {
					return LocalDate.parse(key, DryApiConstants.DATE_FORMATER).atStartOfDay();
				}
			}
		});

	}
}
