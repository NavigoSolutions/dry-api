package com.navigo3.dryapi.core.security.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.util.Consumer3;

public class ObjectFieldsSecurity<TAppContext extends AppContext, TCallContext extends CallContext> {

	private final Consumer3<TAppContext, TCallContext, ObjectFieldsSecurityBuilder<TAppContext, TCallContext>> block;

	public ObjectFieldsSecurity(
		Consumer3<TAppContext, TCallContext, ObjectFieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		this.block = block;
	}

	public ObjectPathsTree getAllowedPaths(TAppContext appContext, TCallContext callContext,
		ObjectPathsTree validPaths) {
		Map<StructurePath, SecurityCheck<TAppContext, TCallContext>> paths = ObjectFieldsSecurityBuilder.build(
			validPaths,
			builder -> {
				block.accept(appContext, callContext, builder);
			}
		);

		List<CacheEntry<TAppContext, TCallContext>> cache = new ArrayList<>();

		List<StructurePath> allowedPaths = paths.entrySet().stream().filter(e -> {
			Optional<CacheEntry<TAppContext, TCallContext>> cacheEntry = cache.stream()
				.filter(c -> c.getSecurityCheck() == e.getValue()) // really, compare objects by identity!
				.findFirst();

			if (cacheEntry.isPresent()) {
				return cacheEntry.get().getPassed();
			}

			boolean passed = e.getValue().pass(appContext, callContext);

			cache.add(
				ImmutableCacheEntry.<TAppContext, TCallContext>builder()
					.securityCheck(e.getValue())
					.passed(passed)
					.build()
			);

			return passed;
		}).map(e -> e.getKey()).collect(Collectors.toList());

		return ObjectPathsTree.from(allowedPaths);
	}
}
