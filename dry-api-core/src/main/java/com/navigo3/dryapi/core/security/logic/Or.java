package com.navigo3.dryapi.core.security.logic;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.security.core.NestedSecurityCheck;
import com.navigo3.dryapi.core.security.core.SecurityCheck;

public class Or<TAppContext extends AppContext, TCallContext extends CallContext> extends NestedSecurityCheck<TAppContext, TCallContext> {
	@SafeVarargs
	public Or(SecurityCheck<TAppContext, TCallContext>...items) {
		super(items);
	}
	
	@Override
	public boolean pass(TAppContext appContext, TCallContext callContext) {
		return getChildren().stream().anyMatch(i->i.pass(appContext, callContext));
	}

	@Override
	public String getDescription() {
		return "or";
	}
}
