package com.navigo3.dryapi.core.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

public class StructurePath {
	private List<StructurePathItem> items = new ArrayList<>();
	
	public static StructurePath empty() {
		return new StructurePath();
	}
	
	public static StructurePath key(String key) {
		Validate.notBlank(key);
		
		return new StructurePath(Optional.empty(), StructurePathItem.createKey(key));
	}
	
	public static StructurePath index(int index) {
		Validate.nonNegative(index);
		
		return new StructurePath(Optional.empty(), StructurePathItem.createIndex(index));
	}
	
	private StructurePath() {
		//
	}

	private StructurePath(Optional<StructurePath> base, StructurePathItem item) {
		Validate.notNull(item);
		
		base.ifPresent(o->items.addAll(o.getItems()));
		items.add(item);
	}

	public String toDebug() {
		return toDebug(getItems().size());
	}
	
	public String toDebug(int maxIndex) {
		return getItems()
			.stream()
			.limit(maxIndex+1)
			.map(i->{
				switch (i.getType()) {
					case KEY: return i.getKey().get();
					case INDEX: return i.getIndex().get().toString();
					default: throw new RuntimeException(StringUtils.subst("Unknown type '{}'", i.getType()));
				}
			})
			.collect(Collectors.joining("."));
	}

	public List<StructurePathItem> getItems() {
		return items;
	}
	
	public StructurePath addKey(String key) {
		Validate.notBlank(key);
		
		return new StructurePath(Optional.of(this), StructurePathItem.createKey(key));
	}
	
	public StructurePath addIndex(int index) {
		Validate.nonNegative(index);
		
		return new StructurePath(Optional.of(this), StructurePathItem.createIndex(index));
	}
}
