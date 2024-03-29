package com.navigo3.dryapi.client;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

import org.immutables.value.Value;

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

	public static ImmutableRemoteHttpDryApiSettings buildDefault() {
		return ImmutableRemoteHttpDryApiSettings.builder().build();
	}
}