package com.navigo3.dryapi.core.meta;

import java.util.Arrays;
import java.util.Collection;
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

	default boolean throwIfPathDoesNotExists(StructurePath path) {
		StringBuilder errorMessage = new StringBuilder();
		
		if (!keyExists(path, Optional.of(errorMessage), true)) {
			throw new RuntimeException(errorMessage.toString());
		}
		
		return true;
	}
	
	default boolean keyExists(StructurePath path) {
		return keyExists(path, Optional.empty(), true);
	}
	
	default boolean keyExistsLenient(StructurePath path) {
		return keyExists(path, Optional.empty(), false);
	}
	
	default boolean keyExists(StructurePath path, Optional<StringBuilder> errorMessage, boolean errorIfNotLeaf) {
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
			
			int fi = i;
			
			if (foundNode.isPresent()) {
				actOptions = foundNode.get().getItems().orElse(Arrays.asList());
			} else {
				errorMessage.ifPresent(b->b.append(StringUtils.subst("Cannot continue at path '{}'", path.toDebug(fi))));
				return false;
			}
			
			++i;
		}
		
		if (errorIfNotLeaf && !actOptions.isEmpty()) {
			List<ObjectPathsTreeNode> fiActOptions = actOptions;
			
			errorMessage.ifPresent(b->b.append(StringUtils.subst(
				"Path [{}] does not select field. Maybe you want continue with:\n{}", 
				path.toDebug(),
				fiActOptions
					.stream()
					.map(node->"\t"+node.toDebug())
					.collect(Collectors.joining("\n"))
			)));
			
			return false;
		}
		
		return true;
	}

	static ObjectPathsTree from(List<StructurePath> allowedPaths) {
		ImmutableObjectPathsTree.Builder builder = ImmutableObjectPathsTree.builder();
		
		builder.addAllItems(groupByIndex(allowedPaths, 0)
			.stream()
			.map(group->createNode(group, 0))
			.collect(Collectors.toList())
		);
		
		return builder.build();
	}

	static ObjectPathsTreeNode createNode(List<StructurePath> paths, int index) {
		Validate.notEmpty(paths);
		
		ImmutableObjectPathsTreeNode.Builder builder = ImmutableObjectPathsTreeNode.builder();
		
		StructurePathItem item = paths.get(0).getItems().get(index);
		
		builder.type(item.getType());
		builder.index(item.getIndex());
		builder.key(item.getKey());
		
		builder.items(groupByIndex(paths, index+1)
			.stream()
			.map(group->createNode(group, index+1))
			.collect(Collectors.toList())
		);
		
		return builder.build();
	}

	static Collection<List<StructurePath>> groupByIndex(List<StructurePath> paths, int index) {
		return paths
			.stream()
			.filter(path->path.getItems().size()>index)
			.collect(Collectors.groupingBy(path->path.getItems().get(index)))
			.values();
	}
}
