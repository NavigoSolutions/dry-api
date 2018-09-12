package com.navigo3.dryapi.core.security.field;

import java.util.Map;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;

public class StaticFieldsSecurity<TAppContext extends AppContext, TCallContext extends CallContext> implements FieldsSecurity<TAppContext, TCallContext> {
	private Map<TypePath, SecurityCheck<TAppContext, TCallContext>> securityPerField;

	public StaticFieldsSecurity(Map<TypePath, SecurityCheck<TAppContext, TCallContext>> securityPerField) {
		this.securityPerField = securityPerField;
	}

	@Override
	public Map<TypePath, SecurityCheck<TAppContext, TCallContext>> getSecurityPerField(TAppContext appContext, TCallContext callContext, TypeSchema schema) {
		return securityPerField;
	}

	@Override
	public boolean canBeCashed() {
		return true;
	}
}
