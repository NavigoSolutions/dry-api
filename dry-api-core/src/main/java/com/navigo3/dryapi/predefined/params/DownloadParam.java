package com.navigo3.dryapi.predefined.params;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.utils.DownloadParamInterface;

@Value.Immutable
@JsonSerialize(as = ImmutableDownloadParam.class)
@JsonDeserialize(as = ImmutableDownloadParam.class)
public interface DownloadParam extends DownloadParamInterface {
	//Must be empty! Only props from DownloadParamInterface are acceptable. Otherwise client will not sent complete data, because it sees only DownloadParamInterface.
}

