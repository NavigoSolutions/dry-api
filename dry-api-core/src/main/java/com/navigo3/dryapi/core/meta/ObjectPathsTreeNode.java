package com.navigo3.dryapi.core.meta;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.path.StructurePathItem.JsonPathItemType;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

@Value.Immutable
@Value.Modifiable
@JsonSerialize(as = ImmutableObjectPathsTreeNode.class)
@JsonDeserialize(as = ImmutableObjectPathsTreeNode.class)
public interface ObjectPathsTreeNode {
	JsonPathItemType getType();

	Optional<String> getKey();

	Optional<Integer> getIndex();

	Optional<List<ObjectPathsTreeNode>> getItems();
	
	@Value.Check default void check() {
		if (getType()==JsonPathItemType.key) {
			Validate.isPresent(getKey());
			Validate.notBlank(getKey().get());
			Validate.notPresent(getIndex());
		} else if (getType()==JsonPathItemType.index) {
			Validate.isPresent(getIndex());
			Validate.notPresent(getKey());
		} else if (getType()==JsonPathItemType.each) {
			Validate.notPresent(getKey());
			Validate.notPresent(getIndex());
		} else {
			throw new RuntimeException(StringUtils.subst("Unknown type {}", getType()));
		}
	}
	
	default String toDebug() {
		if (getType()==JsonPathItemType.key) {
			return StringUtils.subst("\"{}\"", getKey().get());
		} else if (getType()==JsonPathItemType.index) {
			return StringUtils.subst("[{}]", getIndex().get());			
		} else if (getType()==JsonPathItemType.each) {
			return "*";
		} else {
			throw new RuntimeException(StringUtils.subst("Unknown type {}", getType()));
		}
	}
}