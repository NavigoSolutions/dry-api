package com.navigo3.dryapi.core.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.navigo3.dryapi.core.util.Validate;

public class TypePath {
	
	List<TypePathItem> items = new ArrayList<>();
	
	public static TypePath key() {
		return new TypePath(Optional.empty(), ImmutableTypePathItem
			.builder()
			.type(TypeSelectorType.KEY)
			.build()
		);
	}
	
	public static TypePath index() {
		return new TypePath(Optional.empty(), ImmutableTypePathItem
			.builder()
			.type(TypeSelectorType.INDEX)
			.build()
		);
	}

	public static TypePath field(String name) {
		return new TypePath(Optional.empty(), ImmutableTypePathItem
			.builder()
			.type(TypeSelectorType.FIELD)
			.fieldName(name)
			.build()
		);
	}
	
	public static TypePath empty() {
	return new TypePath();
	}
	
	private TypePath() {
		
	}
	
	private TypePath(Optional<List<TypePathItem>> base, TypePathItem item) {
		Validate.notNull(item);
		
		base.ifPresent(list->{
			if (!list.isEmpty()) {
				Validate.notEquals(list.get(list.size()-1).getType(), TypeSelectorType.KEEP_RECURSIVELY, "Trying to extend recursive path!");
			}
		});
		
		base.ifPresent(items::addAll);
		items.add(item);
	}

	public List<TypePathItem> getItems() {
		return items;
	}

	public String getDebug(int maxIndex) {
		return getItems()
			.stream()
			.limit(maxIndex+1)
			.map(i->i.getDebug())
			.collect(Collectors.joining("."));
	}
	
	public TypePath addKey() {
		return new TypePath(Optional.of(items), ImmutableTypePathItem
			.builder()
			.type(TypeSelectorType.KEY)
			.build()
		);
	}
	
	public TypePath addIndex() {
		return new TypePath(Optional.of(items), ImmutableTypePathItem
			.builder()
			.type(TypeSelectorType.INDEX)
			.build()
		);
	}

	public TypePath addField(String name) {
		return new TypePath(Optional.of(items), ImmutableTypePathItem
			.builder()
			.type(TypeSelectorType.FIELD)
			.fieldName(name)
			.build()
		);
	}
	
	public TypePath andRecursively() {
		return new TypePath(Optional.of(items), ImmutableTypePathItem
			.builder()
			.type(TypeSelectorType.KEEP_RECURSIVELY)
			.build()
		);
	}
}
