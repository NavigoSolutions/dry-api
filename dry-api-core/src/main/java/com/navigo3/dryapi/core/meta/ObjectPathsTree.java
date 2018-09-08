package com.navigo3.dryapi.core.meta;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.path.ImmutableStructurePath;
import com.navigo3.dryapi.core.path.ImmutableStructurePath.Builder;
import com.navigo3.dryapi.core.path.StructureSelectorType;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.path.StructurePathItem;
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

	default StructurePath buildPath(Object[] items) {
		List<ObjectPathsTreeNode> actOptions = getItems();
		
		Builder builder = ImmutableStructurePath.builder();

		for (Object item : items) {
			Optional<ObjectPathsTreeNode> foundNode = actOptions
				.stream()
				.filter(node->{
					if (item instanceof Number) {
						if (node.getType()==StructureSelectorType.INDEX) {
							return Objects.equals(node.getIndex().get(), item);
						}
					} else if (item instanceof String) {
						if (node.getType()==StructureSelectorType.KEY) {
							return Objects.equals(node.getKey().get(), item);
						}
					} else {
						throw new RuntimeException("Unsupported key type "+item.getClass().getName());
					}
					
					return false;
				})
				.findFirst();
			
			if (foundNode.isPresent()) {
				if (foundNode.get().getType()==StructureSelectorType.INDEX) {
					builder.addItems(StructurePathItem.createIndex((Integer)item));
				} else if (foundNode.get().getType()==StructureSelectorType.KEY) {
					builder.addItems(StructurePathItem.createKey((String)item));
				} else {
					throw new RuntimeException("Unexpected type "+foundNode.get().getType());
				}
			} else {
				throw new RuntimeException(StringUtils.subst("Cannot continue at item '{}'", item));
			}
		}
		
		return builder.build();
	}
}
