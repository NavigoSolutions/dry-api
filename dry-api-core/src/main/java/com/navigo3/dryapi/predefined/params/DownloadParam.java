package com.navigo3.dryapi.predefined.params;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableDownloadParam.class)
@JsonDeserialize(as = ImmutableDownloadParam.class)
public interface DownloadParam {
	String getMimeType();
	String getName();
	String getContentBase64();
}

