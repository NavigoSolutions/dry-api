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
		return key(key, true);
	}

	/**
	 * 
	 * @param key
	 * @param convertToCamelCase historically this method did not have this
	 *                           parameter and it was automatically converted key
	 *                           into camel case. This feature is helpful for
	 *                           example when names of database fields are being
	 *                           used as validation keys, but it was probably bad
	 *                           idea to implement it directly into dry-api.
	 */
	public static StructurePath key(String key, boolean convertToCamelCase) {
		Validate.notBlank(key);

		return ImmutableStructurePath.builder().addItems(StructurePathItem.createKey(key, convertToCamelCase)).build();
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
		return addKey(key, true);
	}

	/**
	 * 
	 * @param key
	 * @param convertToCamelCase historically this method did not have this
	 *                           parameter and it was automatically converted key
	 *                           into camel case. This feature is helpful for
	 *                           example when names of database fields are being
	 *                           used as validation keys, but it was probably bad
	 *                           idea to implement it directly into dry-api.
	 */
	default StructurePath addKey(String key, boolean convertToCamelCase) {
		Validate.notBlank(key);

		return ImmutableStructurePath.builder()
			.addAllItems(getItems())
			.addItems(StructurePathItem.createKey(key, convertToCamelCase))
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
