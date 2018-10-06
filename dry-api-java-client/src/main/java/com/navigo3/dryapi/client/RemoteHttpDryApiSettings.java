package com.navigo3.dryapi.client;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.util.JacksonUtils.DataFormat;

@Value.Immutable
public interface RemoteHttpDryApiSettings {
	@Value.Default
	default int getMaxExecutedInParallel() {
		return 10;
	}
	
	@Value.Default
	default Duration getConnectionsPoolTimeout() {
		return Duration.ofSeconds(10);
	}
	
	@Value.Default
	default int getMaxPendingCalls() {
		return 1000;
	}
	
	Optional<Consumer<Throwable>> getGlobalErrorHandler();
	
	public static RemoteHttpDryApiSettings buildDefault() {
		return ImmutableRemoteHttpDryApiSettings
			.builder()
			.build();
	}
	
	@Value.Default
	default DataFormat getDataFormat() {
		return DataFormat.JSON;
	}
}