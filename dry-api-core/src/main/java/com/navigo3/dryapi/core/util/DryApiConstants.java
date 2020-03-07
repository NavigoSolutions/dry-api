package com.navigo3.dryapi.core.util;

import java.time.format.DateTimeFormatter;

public class DryApiConstants {
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "HH:mm:ss";
	public static final String DATETIME_FORMAT = DATE_FORMAT+" "+TIME_FORMAT;
	
	public static final DateTimeFormatter DATE_FORMATER = DateTimeFormatter.ofPattern(DATE_FORMAT);
	
	public static final DateTimeFormatter TIME_FORMATER = DateTimeFormatter.ofPattern(TIME_FORMAT);
	
	public static final DateTimeFormatter DATETIME_FORMATER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
	
	public static final String JSON_MIME = "application/json;charset=utf-8";
	
	public static final String REQUEST_SIGNATURE_HEADER = "X-Dry-Api-Req-Signature";
}
