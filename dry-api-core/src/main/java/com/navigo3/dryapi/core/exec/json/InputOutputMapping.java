package com.navigo3.dryapi.core.exec.json;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.path.StructurePath;

@Value.Immutable
@JsonSerialize(as = ImmutableInputOutputMapping.class)
@JsonDeserialize(as = ImmutableInputOutputMapping.class)
public interface InputOutputMapping {
	String getFromUuid();

	StructurePath getFromPath();

	StructurePath getToPath();
}
