package com.navigo3.dryapi.core.path;

import java.util.List;
import java.util.stream.Collectors;

import org.immutables.value.Value;

@Value.Immutable
public interface TypePath {

	List<TypePathItem> getItems();

	default String getDebug(int maxIndex) {
		return getItems()
			.stream()
			.limit(maxIndex+1)
			.map(i->i.getDebug())
			.collect(Collectors.joining("."));
	}
}
