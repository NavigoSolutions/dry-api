package com.navigo3.dryapi.core.path;

import java.util.List;
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

	default String printDebug() {
		return getItems()
			.stream()
			.map(i->{
				switch (i.getType()) {
					case key: return i.getKey().get();
					case index: return i.getIndex().get().toString();
					case each: return "*";
					default: throw new RuntimeException(StringUtils.subst("Unknown type '{}'", i.getType()));
				}
			})
			.collect(Collectors.joining("."));
	}
}
