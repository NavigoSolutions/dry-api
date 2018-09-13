package com.navigo3.dryapi.core.security.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.util.Function3;

public class ObjectFieldsSecurity<TAppContext extends AppContext, TCallContext extends CallContext> implements FieldsSecurity<TAppContext, TCallContext> {

	private final Function3<TAppContext, TCallContext, ObjectPathsTree, Map<StructurePath, SecurityCheck<TAppContext, TCallContext>>> block;

	public ObjectFieldsSecurity(Function3<TAppContext, TCallContext, ObjectPathsTree, Map<StructurePath, SecurityCheck<TAppContext, TCallContext>>> block) {
		this.block = block;
	}
	
	@Override
	public ObjectPathsTree getAllowedPaths(TAppContext appContext, TCallContext callContext, TypeSchema schema, ObjectPathsTree pathsTree) {
		Map<StructurePath, SecurityCheck<TAppContext, TCallContext>> paths = block.apply(appContext, callContext, pathsTree);
		
		List<CacheEntry<TAppContext, TCallContext>> cache = new ArrayList<>();
		
		List<StructurePath> allowedPaths = paths
			.entrySet()
			.stream()
			.filter(e->{
				Optional<CacheEntry<TAppContext, TCallContext>> cacheEntry = cache
					.stream()
					.filter(c->c.getSecurityCheck()==e.getValue()) //really, compare objects by identity!
					.findFirst();
				
				if (cacheEntry.isPresent()) {
					return cacheEntry.get().getPassed();
				}
				
				boolean passed = e.getValue().pass(appContext, callContext);
				
				cache.add(ImmutableCacheEntry.<TAppContext, TCallContext>builder().securityCheck(e.getValue()).passed(passed).build());
				
				return passed;
			})
			.map(e->e.getKey())
			.collect(Collectors.toList());
		
		return ObjectPathsTree.from(allowedPaths);
	}
}
