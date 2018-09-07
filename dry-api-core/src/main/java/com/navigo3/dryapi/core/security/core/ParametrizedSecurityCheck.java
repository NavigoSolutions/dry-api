package com.navigo3.dryapi.core.security.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ParametrizedSecurityCheck<T> {
	List<T> getUsedContextParams();
	
	List<T> getOptionalContextParams();
	
	public static <T> Set<T> recursivelyExtractUsedContextParams(SecurityCheck<?, ?> root, Class<T> klass) {
		Set<ParametrizedSecurityCheck<?>> all = new HashSet<>();
		recursivelyExtractAllParametrized(root, all);
		
		return all
			.stream()
			.flatMap(c->c.getUsedContextParams().stream())
			.map(klass::cast)
			.collect(Collectors.toSet());
	}

	public static <T> Set<T> recursivelyExtractOptionalContextParams(SecurityCheck<?, ?> root, Class<T> klass) {
		Set<ParametrizedSecurityCheck<?>> all = new HashSet<>();
		recursivelyExtractAllParametrized(root, all);
		
		return all
			.stream()
			.flatMap(c->c.getOptionalContextParams().stream())
			.map(klass::cast)
			.collect(Collectors.toSet());		
	}
	
	public static void recursivelyExtractAllParametrized(SecurityCheck<?, ?> item, Set<ParametrizedSecurityCheck<?>> res) {
		if (item instanceof ParametrizedSecurityCheck) {
			res.add((ParametrizedSecurityCheck<?>)item);
		}
		
		if (item instanceof ParentSecurityCheck) {
			((ParentSecurityCheck<?, ?>)item).getChildren().forEach(c->{
				recursivelyExtractAllParametrized(c, res);
			});
		}
	}
}
