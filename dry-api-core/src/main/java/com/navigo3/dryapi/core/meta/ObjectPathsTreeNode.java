package com.navigo3.dryapi.core.meta;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.path.StructureSelectorType;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

@Value.Immutable
@Value.Modifiable
@JsonSerialize(as = ImmutableObjectPathsTreeNode.class)
@JsonDeserialize(as = ImmutableObjectPathsTreeNode.class)
public interface ObjectPathsTreeNode {
	StructureSelectorType getType();

	Optional<String> getKey();

	Optional<Integer> getIndex();

	Optional<List<ObjectPathsTreeNode>> getItems();
	
	@Value.Check default void check() {
		if (getType()==StructureSelectorType.KEY) {
			Validate.isPresent(getKey());
			Validate.notBlank(getKey().get());
			Validate.notPresent(getIndex());
		} else if (getType()==StructureSelectorType.INDEX) {
			Validate.isPresent(getIndex());
			Validate.notPresent(getKey());
		} else {
			throw new RuntimeException(StringUtils.subst("Unknown type {}", getType()));
		}
	}
	
	default String toDebug() {
		if (getType()==StructureSelectorType.KEY) {
			return StringUtils.subst("\"{}\"", getKey().get());
		} else if (getType()==StructureSelectorType.INDEX) {
			return StringUtils.subst("[{}]", getIndex().get());			
		} else {
			throw new RuntimeException(StringUtils.subst("Unknown type {}", getType()));
		}
	}
	
	default void addToPaths(StructurePath basePath, List<StructurePath> res) {
		StructurePath myPath;
		
		if (getType()==StructureSelectorType.KEY) {
			myPath = basePath.addKey(getKey().get());
		} else if (getType()==StructureSelectorType.INDEX) {
			myPath = basePath.addIndex(getIndex().get());
		} else {
			throw new RuntimeException("Unsupported key type "+getType());
		}
		
		if (!getItems().isPresent() || getItems().get().isEmpty()) {
			res.add(myPath);
		} else {
			getItems().get().forEach(node->node.addToPaths(myPath, res));
		}
	}
}