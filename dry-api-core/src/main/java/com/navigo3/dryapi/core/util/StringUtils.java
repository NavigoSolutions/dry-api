package com.navigo3.dryapi.core.util;

import java.util.regex.Matcher;

public class StringUtils {
	
	public static String subst(String pattern, Object...args) {
		String res = pattern;

		for (Object arg : args) {
			res = res.replaceFirst("\\{\\}", Matcher.quoteReplacement(arg.toString()));
		}
		
		return res;
	}
	
	public static String repeat(int count, String pattern) {
		Validate.nonNegative(count);
		Validate.notNull(pattern);
		
		StringBuilder res = new StringBuilder();
		
		IntegerUtils.times(count, i->res.append(pattern));
		
		return res.toString();
	}
	
	public static String withFirstLowercase(String str) {
		if (str.isEmpty()) {
			return "";
		} else if (str.length()==1) {
			return str.toLowerCase();
		} else {
			return str.substring(0, 1).toLowerCase() + str.substring(1);
		}
	}
}
