package com.navigo3.dryapi.core.meta;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.util.StringUtils;

@Value.Immutable

@JsonSerialize(as = ImmutableObjectPathsTree.class)
@JsonDeserialize(as = ImmutableObjectPathsTree.class)
public interface ObjectPathsTree {
	List<ObjectPathsTreeNode> getItems();
	
	default void printDebug() {
		System.out.println("->");
		getItems().forEach(n->printDebug(1, n));
	}

	default void printDebug(int level, ObjectPathsTreeNode node) {
		System.out.println(StringUtils.repeat(level, "  ")+node.toDebug());
		
		node.getItems().ifPresent(items->{
			items.forEach(i->{
				printDebug(level+1, i);
			});
		});
	}
}
