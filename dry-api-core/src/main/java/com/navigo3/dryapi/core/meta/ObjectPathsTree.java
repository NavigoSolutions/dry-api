package com.navigo3.dryapi.core.meta;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.path.StructurePathItem;
import com.navigo3.dryapi.core.path.StructureSelectorType;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

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

	default void throwIfPathDoesNotExists(StructurePath path) {
		List<ObjectPathsTreeNode> actOptions = getItems();
		
		int i = 0;
		
		for (StructurePathItem item : path.getItems()) {
			Optional<ObjectPathsTreeNode> foundNode = actOptions
				.stream()
				.filter(node->{
					if (node.getType()==item.getType()) {
						if (node.getType()==StructureSelectorType.KEY) {
							return node.getKey().get().equals(item.getKey().get());
						} else if (node.getType()==StructureSelectorType.INDEX) {
							return node.getIndex().get().equals(item.getIndex().get());
						} else {
							throw new RuntimeException("Unsupported key type "+node.getType());
						}
					} else {
						return true;
					}
				})
				.findFirst();
			
			if (foundNode.isPresent()) {
				actOptions = foundNode.get().getItems().orElse(Arrays.asList());
			} else {
				throw new RuntimeException(StringUtils.subst("Cannot continue at path '{}'", path.toDebug(i)));
			}
			
			++i;
		}
		
		Validate.isEmpty(actOptions, 
			StringUtils.subst(
				"Path [{}] does not select field. Maybe you want continue with:\n{}", 
				path.toDebug(),
				actOptions
					.stream()
					.map(node->"\t"+node.toDebug())
					.collect(Collectors.joining("\n"))
			)
		);
	}
}
