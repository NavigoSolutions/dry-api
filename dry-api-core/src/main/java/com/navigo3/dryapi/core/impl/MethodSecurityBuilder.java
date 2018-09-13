package com.navigo3.dryapi.core.impl;

import java.util.function.Consumer;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.security.field.ObjectFieldsSecurityBuilder;
import com.navigo3.dryapi.core.security.field.TypeFieldsSecurityBuilder;
import com.navigo3.dryapi.core.util.Validate;

public class MethodSecurityBuilder<TAppContext extends AppContext, TCallContext extends CallContext> {
		
	private final ImmutableMethodSecurity.Builder<TAppContext, TCallContext> builder = ImmutableMethodSecurity.builder();
	private final TypeSchema inputSchema;
	private final TypeSchema outputSchema;
	
	public MethodSecurityBuilder(TypeSchema inputSchema, TypeSchema outputSchema) {
		Validate.notNull(inputSchema);
		Validate.notNull(outputSchema);
		
		this.inputSchema = inputSchema;
		this.outputSchema = outputSchema;
	}

	public MethodSecurityBuilder<TAppContext, TCallContext> authorization(SecurityCheck<TAppContext, TCallContext> authorization) {
		builder.authorization(authorization);
		
		return this;
	}
	
	public MethodSecurity<TAppContext, TCallContext> build() {
		return builder.build();
	}
	
	public MethodSecurityBuilder<TAppContext, TCallContext> defineInputFieldsTypeSecurity(Consumer<TypeFieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		Validate.notNull(block);
		
		builder.inputFieldsTypeSecurity(TypeFieldsSecurityBuilder.build(inputSchema, block));

		return this;
	}
	
	public MethodSecurityBuilder<TAppContext, TCallContext> defineOutputFieldsTypeSecurity(Consumer<TypeFieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		Validate.notNull(block);
		
		builder.outputFieldsTypeSecurity(TypeFieldsSecurityBuilder.build(outputSchema, block));
		
		return this;
	}
	
	public MethodSecurityBuilder<TAppContext, TCallContext> defineInputFieldsObjectSecurity(Consumer<ObjectFieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		Validate.notNull(block);
		
		builder.inputFieldsObjectSecurity(block);

		return this;
	}
	
	public MethodSecurityBuilder<TAppContext, TCallContext> defineOutputObjectSecurity(Consumer<ObjectFieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		Validate.notNull(block);
		
		builder.outputFieldsObjectSecurity(block);
		
		return this;
	}
}
