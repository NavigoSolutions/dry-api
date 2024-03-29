package com.navigo3.dryapi.core.util;

import java.text.Normalizer;
import java.util.Optional;
import java.util.regex.Matcher;

public class StringUtils {

	public static String subst(String pattern, Object... args) {
		String res = pattern;

		for (Object arg : args) {
			res = res.replaceFirst("\\{\\}", Matcher.quoteReplacement(arg != null ? arg.toString() : "NULL"));
		}

		return res;
	}

	public static String repeat(int count, String pattern) {
		Validate.nonNegative(count);
		Validate.notNull(pattern);

		StringBuilder res = new StringBuilder();

		IntegerUtils.times(count, i -> res.append(pattern));

		return res.toString();
	}

	public static String withFirstLowercase(String str) {
		if (str.isEmpty()) {
			return "";
		} else if (str.length() == 1) {
			return str.toLowerCase();
		} else {
			return str.substring(0, 1).toLowerCase() + str.substring(1);
		}
	}

	public static String withFirstLowercaseAndRestLowercase(String str) {
		if (str.isEmpty()) {
			return "";
		} else if (str.length() == 1) {
			return str.toLowerCase();
		} else {
			return str.substring(0, 1).toLowerCase() + str.substring(1).toLowerCase();
		}
	}

	public static String withFirstUppercaseAndRestLowercase(String str) {
		if (str.isEmpty()) {
			return "";
		} else if (str.length() == 1) {
			return str.toUpperCase();
		} else {
			return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
		}
	}

	public static String underscoreToCamelCase(String value) {
		Validate.notNull(value);

		if (!value.contains("_")) {
			return value;
		}

		String[] items = value.split("_");

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < items.length; ++i) {
			String item = items[i];

			if (item.matches("[0-9].*")) {
				builder.append("_" + item.toLowerCase());
			} else {
				builder.append(
					i == 0 ? withFirstLowercaseAndRestLowercase(item) : withFirstUppercaseAndRestLowercase(item)
				);
			}
		}

		return builder.toString();
	}

	public static String defaultString(String str) {
		return str == null ? "" : str;
	}

	public static boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isBlank(Optional<String> str) {
		return str.map(StringUtils::isBlank).orElse(true);
	}

	public static String stripAccents(String s) {
		s = Normalizer.normalize(s, Normalizer.Form.NFD);

		s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

		// replace a few common characters without decomposition mapping
		s = s.replaceAll("\\u0141", "L");
		s = s.replaceAll("\\u0142", "l");

		return s;
	}

	public static String toAscii(String s) {
		return stripAccents(s).replaceAll("[^\\p{ASCII}]", "");
	}
}
