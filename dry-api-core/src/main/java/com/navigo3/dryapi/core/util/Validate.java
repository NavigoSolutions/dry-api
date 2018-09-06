package com.navigo3.dryapi.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Validate {
	public static <T> void keyNotContained(Map<T, ?> map, T key) {
		if (map.containsKey(key)) {
			throw new RuntimeException(StringUtils.subst("Key {} should not be contained in this map", key));
		}
	}

	public static void isPresent(Optional<?> optional) {
		if (!optional.isPresent()) {
			throw new RuntimeException("Optional not presented");
		}
	}

	public static void isPresent(Optional<?> optional, String message, Object... args) {
		if (!optional.isPresent()) {
			throw new RuntimeException(StringUtils.subst(message, args));
		}
	}

	public static void notPresent(Optional<?> optional) {
		if (optional.isPresent()) {
			throw new RuntimeException("Optional is presented but shouldn't");
		}
	}

	public static <T> void sameInstance(T a, T b) {
		if (a!=b) {
			throw new RuntimeException("Objects are not same instance");
		}
	}
	
	public static void passRegex(String value, String regex) {
		if (!value.matches(regex)) {
			throw new RuntimeException(StringUtils.subst("Value '{}' does not match regex '{}'", value, regex));
		}
	}

	public static void notNull(Object o) {
		if (o==null) {
			throw new RuntimeException("This object should not be null");
		}
	}

	public static void isNull(Object o) {
		if (o!=null) {
			throw new RuntimeException("This object should be null");
		}
	}

	public static void notBlank(String val) {
		if (val==null || val.trim().isEmpty()) {
			throw new RuntimeException("This string should not be blank");
		}
	}

	public static void notEmpty(Collection<?> coll) {
		if (coll.isEmpty()) {
			throw new RuntimeException("This collection should not be empty");
		}
	}

	public static void isTrue(boolean val) {
		isTrue(val, "Value 'true' expected");
	}

	public static void isTrue(boolean val, String message) {
		if (!val) {
			throw new RuntimeException(message);
		}
	}

	public static void isFalse(boolean val) {
		if (val) {
			throw new RuntimeException("Value 'false' expected");
		}
	}

	public static void isFalse(boolean val, String message) {
		if (val) {
			throw new RuntimeException(message);
		}
	}

	public static void nonNegative(int val) {
		if (val<0) {
			throw new RuntimeException(StringUtils.subst("Non negative value expected, got {}", val));
		}
	}

	public static void greaterThanZero(int val) {
		if (val<=0) {
			throw new RuntimeException(StringUtils.subst("Expected value greather than zero, got {}", val));
		}
	}

	public static void equals(Object a, Object b, String message) {
		if (!Objects.equals(a, b)) {
			throw new RuntimeException(message);
		}
	}

	public static <T> void notContained(Collection<T> coll, T val) {
		if (coll.contains(val)) {
			throw new RuntimeException(StringUtils.subst("Value {} is already contained", val));
		}
	}

	public static <T, U> void hasUniqueProperty(Collection<T> coll, Function<T, U> selector) {
		Set<U> vals = new HashSet<>();
		
		coll.forEach(i->vals.add(selector.apply(i)));
		
		if (vals.size()!=coll.size()) {
			throw new RuntimeException("Collection contains duplicate field values");
		}
	}
}
