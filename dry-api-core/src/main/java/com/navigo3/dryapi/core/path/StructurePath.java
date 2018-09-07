package com.navigo3.dryapi.core.path;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.util.StringUtils;

@Value.Immutable
@JsonSerialize(as = ImmutableStructurePath.class)
@JsonDeserialize(as = ImmutableStructurePath.class)
public interface StructurePath {
	List<StructurePathItem> getItems();

	default String toDebug() {
		return getItems()
			.stream()
			.map(i->{
				switch (i.getType()) {
					case key: return i.getKey().get();
					case index: return i.getIndex().get().toString();
					default: throw new RuntimeException(StringUtils.subst("Unknown type '{}'", i.getType()));
				}
			})
			.collect(Collectors.joining("."));
	}
	
	public static StructurePath of(Consumer<StructurePathBuilder> block) {
		StructurePathBuilder builder = StructurePathBuilder.create();
		
		block.accept(builder);
		
		return builder.build();
	}
	
	default StructurePath appendKey(String key) {
		return ImmutableStructurePath
			.builder()
			.addAllItems(getItems())
			.addItems(StructurePathItem.createKey(key))
			.build();
	}
	
	default StructurePath appendIndex(int index) {
		return ImmutableStructurePath
			.builder()
			.addAllItems(getItems())
			.addItems(StructurePathItem.createIndex(index))
			.build();
	}
}
