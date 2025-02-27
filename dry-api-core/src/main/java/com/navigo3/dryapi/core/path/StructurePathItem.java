package com.navigo3.dryapi.core.path;

import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

@Value.Immutable
@JsonSerialize(as = ImmutableStructurePathItem.class)
@JsonDeserialize(as = ImmutableStructurePathItem.class)
public interface StructurePathItem {

	/**
	 * 
	 * @param key
	 * @param convertToCamelCase historically this method did not have this
	 *                           parameter and it was automatically converted key
	 *                           into camel case. This feature is helpful for
	 *                           example when names of database fields are being
	 *                           used as validation keys, but it was probably bad
	 *                           idea to implement it directly into dry-api.
	 */
	@Deprecated
	static StructurePathItem createKey(String key, boolean convertToCamelCase) {
		Validate.notBlank(key);

		String resultKey = key;

		if (convertToCamelCase) {
			resultKey = StringUtils.underscoreToCamelCase(key);
		}

		return ImmutableStructurePathItem.builder().type(StructureSelectorType.KEY).key(resultKey).build();
	}

	static StructurePathItem createIndex(int index) {
		Validate.nonNegative(index);

		return ImmutableStructurePathItem.builder().type(StructureSelectorType.INDEX).index(index).build();
	}

	StructureSelectorType getType();

	Optional<String> getKey();

	Optional<Integer> getIndex();

	@Value.Check
	default void check() {
		if (getType() == StructureSelectorType.KEY) {
			Validate.isPresent(getKey());
			Validate.notBlank(getKey().get());
			Validate.notPresent(getIndex());
		} else if (getType() == StructureSelectorType.INDEX) {
			Validate.isPresent(getIndex());
			Validate.notPresent(getKey());
		} else {
			throw new RuntimeException(StringUtils.subst("Unknown type {}", getType()));
		}
	}

	default String toDebug() {
		if (getType() == StructureSelectorType.KEY) {
			return StringUtils.subst("\"{}\"", getKey().get());
		} else if (getType() == StructureSelectorType.INDEX) {
			return StringUtils.subst("[{}]", getIndex().get());
		} else {
			throw new RuntimeException(StringUtils.subst("Unknown type {}", getType()));
		}
	}
}
