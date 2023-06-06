package com.navigo3.dryapi.core.path;

import java.util.List;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

@Value.Immutable
@JsonSerialize(as = ImmutableStructurePath.class)
@JsonDeserialize(as = ImmutableStructurePath.class)
public interface StructurePath {

	public static StructurePath empty() {
		return ImmutableStructurePath.builder().build();
	}

	public static StructurePath key(String key) {
		Validate.notBlank(key);

		return ImmutableStructurePath.builder().addItems(StructurePathItem.createKey(key)).build();
	}

	public static StructurePath index(int index) {
		Validate.nonNegative(index);

		return ImmutableStructurePath.builder().addItems(StructurePathItem.createIndex(index)).build();
	}

	List<StructurePathItem> getItems();

	default String toDebug() {
		return toDebug(getItems().size());
	}

	default String toDebug(int maxIndex) {
		return getItems().stream().limit(maxIndex + 1).map(i -> {
			switch (i.getType()) {
			case KEY:
				return i.getKey().get();
			case INDEX:
				return i.getIndex().get().toString();
			default:
				throw new RuntimeException(StringUtils.subst("Unknown type '{}'", i.getType()));
			}
		}).collect(Collectors.joining("."));
	}

	default StructurePath addKey(String key) {
		Validate.notBlank(key);

		return ImmutableStructurePath.builder()
			.addAllItems(getItems())
			.addItems(StructurePathItem.createKey(key))
			.build();
	}

	default StructurePath addIndex(int index) {
		Validate.nonNegative(index);

		return ImmutableStructurePath.builder()
			.addAllItems(getItems())
			.addItems(StructurePathItem.createIndex(index))
			.build();
	}
}
