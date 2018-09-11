package com.navigo3.dryapi.core.path;

import java.util.Optional;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

@Value.Immutable
public interface TypePathItem {
	TypeSelectorType getType();

	Optional<String> getFieldName();
	
	@Value.Check default void check() {
		if (getType()==TypeSelectorType.FIELD) {
			Validate.notBlank(getFieldName());
		}
	}

	default String getDebug() {
		switch (getType()) {
			case INDEX: return "INDEX";
			case KEY: return "KEY";
			case FIELD: return StringUtils.subst("FIELD[{}]", getFieldName().get());
			case KEEP_RECURSIVELY: return "*";
			default: throw new RuntimeException(StringUtils.subst("Unknown type {}", getType()));
		}
	}
}
