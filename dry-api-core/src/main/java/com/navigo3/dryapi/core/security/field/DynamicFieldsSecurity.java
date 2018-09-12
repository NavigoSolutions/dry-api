package com.navigo3.dryapi.core.security.field;

import java.util.Map;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.util.Function3;

public class DynamicFieldsSecurity<TAppContext extends AppContext, TCallContext extends CallContext> implements FieldsSecurity<TAppContext, TCallContext> {

	private final Function3<TAppContext, TCallContext, TypeSchema, Map<TypePath, SecurityCheck<TAppContext, TCallContext>>> block;

	public DynamicFieldsSecurity(Function3<TAppContext, TCallContext, TypeSchema, Map<TypePath, SecurityCheck<TAppContext, TCallContext>>> block) {
		this.block = block;
	}
	
	@Override
	public Map<TypePath, SecurityCheck<TAppContext, TCallContext>> getSecurityPerField(TAppContext appContext, TCallContext callContext, TypeSchema schema) {
		return block.apply(appContext, callContext, schema);
	}

	@Override
	public boolean canBeCashed() {
		return false;
	}

}
