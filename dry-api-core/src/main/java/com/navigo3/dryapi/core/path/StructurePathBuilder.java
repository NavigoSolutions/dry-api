package com.navigo3.dryapi.core.path;

import java.util.ArrayList;
import java.util.List;

import com.navigo3.dryapi.core.util.Validate;

public class StructurePathBuilder {
	public static StructurePathBuilder create() {
		return new StructurePathBuilder();
	}

	private List<StructurePathItem> items = new ArrayList<>();
	
	public StructurePathBuilder key(String key) {
		Validate.notBlank(key);
		
		items.add(StructurePathItem.createKey(key));
		
		return this;
	}
	
	public StructurePathBuilder index(int index) {
		items.add(StructurePathItem.createIndex(index));
		
		return this;
	}
	
	public StructurePathBuilder each() {
		items.add(StructurePathItem.createEach());
		
		return this;
	}
	
	public StructurePath build() {
		Validate.notEmpty(items);
		
		return ImmutableStructurePath
			.builder()
			.items(items)
			.build();
	}
}
