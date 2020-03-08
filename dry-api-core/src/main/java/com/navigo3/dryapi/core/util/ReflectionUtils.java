package com.navigo3.dryapi.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionUtils {
	private static final Pattern genericDeclaration = Pattern.compile("^([^<]+)<(.*)>$");
	
	public static <T> T createInstance(Class<T> klass) {
		try {
			return klass.getConstructor().newInstance();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public static Optional<String> convertGetterToCamelcaseField(String name) {
		if (name.matches("^get[\\p{Lu}0-9].*$")) {
			return Optional.of(StringUtils.withFirstLowercase(name.replaceFirst("get", "")));
		} else if (name.matches("^is[\\p{Lu}0-9].*$")) {
			return Optional.of(StringUtils.withFirstLowercase(name.replaceFirst("is", "")));
		} else if (name.matches("^has[\\p{Lu}0-9].*$")) {
			return Optional.of(StringUtils.withFirstLowercase(name.replaceFirst("has", "")));
		} else {
			return Optional.empty();
		}
	}
	
	public static List<String> parseTemplateParams(String returnTypeDesc) {
		Matcher match = genericDeclaration.matcher(returnTypeDesc.trim());
		
		List<String> items = new ArrayList<>();
		
		if (match.matches()) {
			int openBrackets = 0;
			int lastStart = 0;
			
			for (int i=1;i<match.group(2).length();++i) {
				char c = match.group(2).charAt(i);
				
				if (c=='<') {
					++openBrackets;
				} else if (c=='>') {
					Validate.greaterThanZero(openBrackets);
					--openBrackets;
				} else if (c==',' && openBrackets==0) {
					items.add(match.group(2).substring(lastStart, i).trim());
					lastStart = i+1;
				}
			}
			
			Validate.equals(0, openBrackets, "Unbalanced sharp brackets!");
			
			if (match.group(2).length()-lastStart>0) {
				items.add(match.group(2).substring(lastStart, match.group(2).length()).trim());
			}
	
			
		}
		
		return items;
	}
}
