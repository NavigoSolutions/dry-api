package com.navigo3.dryapi.client;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.immutables.value.Value;

@Value.Immutable
public interface RequestsBatchData {
	@SuppressWarnings("rawtypes")
	List<ModifiableRequestData> getRequests();
	Optional<Consumer<Throwable>> getOnFail();
	
	@Value.Default default CompletableFuture<RequestsBatchData> getFuture() {
		return new CompletableFuture<RequestsBatchData>();
	}
}
