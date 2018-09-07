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
	static StructurePathItem createKey(String key) {
		Validate.notBlank(key);
		
		String camelCaseKey = StringUtils.underscoreToCamelCase(key);
		
		return ImmutableStructurePathItem.builder().type(StructurePathItemType.key).key(camelCaseKey).build();
	}
	
	static StructurePathItem createIndex(int index) {
		Validate.nonNegative(index);
		
		return ImmutableStructurePathItem.builder().type( StructurePathItemType.index).index(index).build();
	}

	StructurePathItemType getType();

	Optional<String> getKey();

	Optional<Integer> getIndex();
	
	@Value.Check default void check() {
		if (getType()==StructurePathItemType.key) {
			Validate.isPresent(getKey());
			Validate.notBlank(getKey().get());
			Validate.notPresent(getIndex());
		} else if (getType()==StructurePathItemType.index) {
			Validate.isPresent(getIndex());
			Validate.notPresent(getKey());
		} else {
			throw new RuntimeException(StringUtils.subst("Unknown type {}", getType()));
		}
	}

	default String toDebug() {
		if (getType()==StructurePathItemType.key) {
			return StringUtils.subst("\"{}\"", getKey().get());
		} else if (getType()==StructurePathItemType.index) {
			return StringUtils.subst("[{}]", getIndex().get());			
		} else {
			throw new RuntimeException(StringUtils.subst("Unknown type {}", getType()));
		}
	}
}
