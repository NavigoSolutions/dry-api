package com.navigo3.dryapi.core.path;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

public class TypePathBuilder {
	public static TypePath of(Consumer<TypePathBuilder> block) {
		TypePathBuilder builder = new TypePathBuilder();
		
		block.accept(builder);
		
		return builder.build();
	}
	
	private List<TypePathItem> items = new ArrayList<>();

	private TypePath build() {
		Validate.notEmpty(items);
		
		return ImmutableTypePath.builder().items(items).build();
	}
	
	public TypePathBuilder field(String name) {
		Validate.notBlank(name);
		
		items.add(ImmutableTypePathItem.builder().type(TypeSelectorType.FIELD).fieldName(StringUtils.underscoreToCamelCase(name)).build());
		
		return this;
	}
	
	public TypePathBuilder key() {
		items.add(ImmutableTypePathItem.builder().type(TypeSelectorType.KEY).build());
		
		return this;
	}
	
	public TypePathBuilder index() {
		items.add(ImmutableTypePathItem.builder().type(TypeSelectorType.INDEX).build());
		
		return this;
	}
	
}
