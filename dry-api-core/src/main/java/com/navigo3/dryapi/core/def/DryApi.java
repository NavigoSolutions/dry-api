package com.navigo3.dryapi.core.def;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

public class DryApi<TAppContext extends AppContext, TCallContext extends CallContext> {
	
	public static final String IDENTIFIER_PATTERN = "[a-z](([A-Za-z0-9]-)*[A-Za-z0-9])*";
	public static final String PATH_PATTERN = StringUtils.subst("({}/)*({})", IDENTIFIER_PATTERN, IDENTIFIER_PATTERN);
	
	@Value.Immutable
	public interface Entry {
		@SuppressWarnings("rawtypes")
		public MethodDefinition getDefinition();
		
		@SuppressWarnings("rawtypes")
		public Class<? extends MethodImplementation> getImplementationClass();
	}

	@SuppressWarnings("rawtypes")
	public <TInput, TOutput> void register(
			Class<? extends MethodDefinition<TInput, TOutput>> defClass, 
			Class<? extends MethodImplementation<TInput, TOutput, TAppContext, TCallContext>> implClass
	) {
		Validate.notNull(defClass);
		Validate.notNull(implClass);
		
		MethodDefinition definition = ReflectionUtils.createInstance(defClass);
		definition.initialize();
		
		String qualifiedName = definition.getQualifiedName();

		Validate.passRegex(qualifiedName, PATH_PATTERN);
		Validate.keyNotContained(entries, qualifiedName);
		
		entries.put(qualifiedName, ImmutableEntry
			.builder()
			.definition(definition)
			.implementationClass(implClass)
			.build()
		);
	}

	private Map<String, Entry> entries = new HashMap<>();
	
	@SuppressWarnings("rawtypes")
	public Optional<MethodDefinition> lookupDefinition(String qualifiedName) {
		return Optional.ofNullable(entries.get(qualifiedName)).map(Entry::getDefinition);
	}
	
	@SuppressWarnings("rawtypes")
	public Optional<Class<? extends MethodImplementation>> lookupImplementationClass(String qualifiedName) {
		return Optional.ofNullable(entries.get(qualifiedName)).map(Entry::getImplementationClass);
	}
}
