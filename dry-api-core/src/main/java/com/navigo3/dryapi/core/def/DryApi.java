package com.navigo3.dryapi.core.def;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.ImmutableEntry.Builder;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodSecurity;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.Validator;

public class DryApi<TAppContext extends AppContext, TCallContext extends CallContext, TValidator extends Validator> {
	
	public static final String IDENTIFIER_PATTERN = "[a-z](([A-Za-z0-9]-)*[A-Za-z0-9])*";
	public static final String PATH_PATTERN = StringUtils.subst("({}/)*({})", IDENTIFIER_PATTERN, IDENTIFIER_PATTERN);
	
	@Value.Immutable
	public interface Entry<TAppContext extends AppContext, TCallContext extends CallContext> {
		@SuppressWarnings("rawtypes")
		MethodDefinition getDefinition();
		
		@SuppressWarnings("rawtypes")
		Class<? extends MethodImplementation> getImplementationClass();
		
		MethodSecurity<TAppContext, TCallContext> getSecurity();
	}

	private Map<String, Entry<TAppContext, TCallContext>> entries = new HashMap<>();

	public <TInput, TOutput> void register(
			MethodDefinition<TInput, TOutput> definition, 
			Class<? extends MethodImplementation<TInput, TOutput, ? extends MethodDefinition<TInput, TOutput>, TAppContext, TCallContext, TValidator>> implClass
	) {
		Validate.notNull(definition);
		Validate.notNull(implClass);

		definition.initialize();
		
		String qualifiedName = definition.getQualifiedName();

		Validate.passRegex(qualifiedName, PATH_PATTERN);
		Validate.keyNotContained(entries, qualifiedName);
		
		MethodImplementation<TInput, TOutput, ? extends MethodDefinition<TInput, TOutput>, TAppContext, TCallContext, TValidator> implementation = 
				ReflectionUtils.createInstance(implClass);
		
		Validate.notNull(implementation);
		
		implementation.setDefinition(definition);
		
		Builder<TAppContext, TCallContext> builder = ImmutableEntry
			.<TAppContext, TCallContext>builder()
			.definition(definition)
			.implementationClass(implClass);
		
		MethodSecurityBuilder<TAppContext, TCallContext> methodBuilder = new MethodSecurityBuilder<>(definition.getInputSchema(), definition.getOutputSchema());
		
		implementation.fillClassSecurity(methodBuilder);
		
		MethodSecurity<TAppContext, TCallContext> security = methodBuilder.build();
		
		builder.security(security);
		
		implementation.setSecurity(security);
		
		entries.put(qualifiedName, builder.build());
	}
	
	@SuppressWarnings("rawtypes")
	public Optional<MethodDefinition> lookupDefinition(String qualifiedName) {
		return Optional.ofNullable(entries.get(qualifiedName)).map(Entry::getDefinition);
	}
	
	@SuppressWarnings("rawtypes")
	public Optional<Class<? extends MethodImplementation>> lookupImplementationClass(String qualifiedName) {
		return Optional.ofNullable(entries.get(qualifiedName)).map(Entry::getImplementationClass);
	}
	
	public Optional<MethodSecurity<TAppContext, TCallContext>> lookupSecurity(String qualifiedName) {
		return Optional.ofNullable(entries.get(qualifiedName)).map(Entry::getSecurity);
	}
	
	public List<String> getAllQualifiedNames() {
		return entries
			.keySet()
			.stream()
			.sorted()
			.collect(Collectors.toList());
	}
}
