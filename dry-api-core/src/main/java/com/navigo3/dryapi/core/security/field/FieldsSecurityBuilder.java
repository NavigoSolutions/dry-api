package com.navigo3.dryapi.core.security.field;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.util.Validate;

public class FieldsSecurityBuilder<TAppContext extends AppContext, TCallContext extends CallContext> {
	public static <TAppContext extends AppContext, TCallContext extends CallContext> FieldsSecurity<TAppContext, TCallContext> 
		build(TypeSchema schema, Consumer<FieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		FieldsSecurityBuilder<TAppContext, TCallContext> builder = new FieldsSecurityBuilder<>(schema);
		
		block.accept(builder);
		
		return builder.build();
	}

	private final TypeSchema schema;
	
	private Map<TypePath, SecurityCheck<TAppContext, TCallContext>> securityPerField = new HashMap<>();

	private FieldsSecurityBuilder(TypeSchema schema) {
		this.schema = schema;
	}
	
	private FieldsSecurity<TAppContext, TCallContext> build() {
		return ImmutableFieldsSecurity
			.<TAppContext, TCallContext>builder()
			.securityPerField(securityPerField)
			.build();
	}
	
	public void add(TypePath path, SecurityCheck<TAppContext, TCallContext> security) {
		Validate.notNull(path);
		Validate.notNull(security);
		Validate.keyNotContained(securityPerField, path);
		
		schema.throwIfPathNotExists(path);
		
		securityPerField.put(path, security);
	}
	
	public void throwIfNotFullyCovered() {
		System.err.println("NOT IMPLEMENTED!!!!");
	}
}
