package com.navigo3.dryapi.core.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class DateTimeXmlKeyModule extends SimpleModule {
	private static final long serialVersionUID = 1L;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "HH-mm-ss";
	public static final String DATETIME_FORMAT = DATE_FORMAT+"_"+TIME_FORMAT;
	
	public static final DateTimeFormatter DATE_FORMATER = DateTimeFormatter.ofPattern(DATE_FORMAT);
	
	public static final DateTimeFormatter TIME_FORMATER = DateTimeFormatter.ofPattern(TIME_FORMAT);
	
	public static final DateTimeFormatter DATETIME_FORMATER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
	
	public DateTimeXmlKeyModule() {
	
		addKeySerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
	        @Override
	        public void serialize(LocalDate date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) 
	        		throws IOException, JsonProcessingException {
	            jsonGenerator.writeFieldName("d_"+DATE_FORMATER.format(date));
	        }
	    });
		
		addKeySerializer(LocalTime.class, new JsonSerializer<LocalTime>() {
	        @Override
	        public void serialize(LocalTime date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) 
	        		throws IOException, JsonProcessingException {
	            jsonGenerator.writeFieldName("t_"+TIME_FORMATER.format(date));
	        }
	    });
		
		addKeySerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
	        @Override
	        public void serialize(LocalDateTime date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) 
	        		throws IOException, JsonProcessingException {
	            jsonGenerator.writeFieldName("dt_"+DATETIME_FORMATER.format(date));
	        }
	    });
	    
		addKeyDeserializer(LocalDate.class, new KeyDeserializer() {

			@Override
			public LocalDate deserializeKey(String key, DeserializationContext ctxt)
					throws IOException, JsonProcessingException {
				
				if (StringUtils.isBlank(key)) {
					return null;
				}
				
				if (key.startsWith("dt_")) {
					return LocalDateTime.parse(key.substring(3), DATETIME_FORMATER).toLocalDate();
				} else {
					return LocalDate.parse(key.substring(2), DATE_FORMATER);
				}
			}
		});
	    
		addKeyDeserializer(LocalTime.class, new KeyDeserializer() {

			@Override
			public LocalTime deserializeKey(String key, DeserializationContext ctxt)
					throws IOException, JsonProcessingException {
				
				if (StringUtils.isBlank(key)) {
					return null;
				}
				
				return LocalTime.parse(key.substring(2), TIME_FORMATER);
			}
		});
	    
		addKeyDeserializer(LocalDateTime.class, new KeyDeserializer() {

			@Override
			public LocalDateTime deserializeKey(String key, DeserializationContext ctxt)
					throws IOException, JsonProcessingException {
				
				if (StringUtils.isBlank(key)) {
					return null;
				}
				
				if (key.startsWith("dt_")) {
					return LocalDateTime.parse(key.substring(3), DATETIME_FORMATER);
				} else {
					return LocalDate.parse(key.substring(2), DATE_FORMATER).atStartOfDay();
				}
			}
		});
		
	}
}
