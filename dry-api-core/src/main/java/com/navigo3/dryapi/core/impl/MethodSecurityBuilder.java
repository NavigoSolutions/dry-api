package com.navigo3.dryapi.core.impl;

import java.util.function.Consumer;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.security.field.FieldsSecurity;
import com.navigo3.dryapi.core.security.field.FieldsSecurityBuilder;
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
	
	public MethodSecurityBuilder<TAppContext, TCallContext> defineInputFieldsSecurity(Consumer<FieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		Validate.notNull(block);
		
		builder.inputFieldsSecurity(FieldsSecurityBuilder.build(inputSchema, block));

		return this;
	}
	
	public MethodSecurityBuilder<TAppContext, TCallContext> defineOutputFieldsSecurity(Consumer<FieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		Validate.notNull(block);
		
		builder.outputFieldsSecurity(FieldsSecurityBuilder.build(outputSchema, block));
		
		return this;
	}
	
	public MethodSecurityBuilder<TAppContext, TCallContext> inputFieldsSecurity(FieldsSecurity<TAppContext, TCallContext> security) {
		Validate.notNull(security);
		
		builder.inputFieldsSecurity(security);

		return this;
	}
	
	public MethodSecurityBuilder<TAppContext, TCallContext> outputFieldsSecurity(FieldsSecurity<TAppContext, TCallContext> security) {
		Validate.notNull(security);
		
		builder.outputFieldsSecurity(security);

		return this;
	}
}
