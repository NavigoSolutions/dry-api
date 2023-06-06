package com.navigo3.dryapi.core.security.logic;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.security.core.NestedSecurityCheck;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.util.Validate;

public class Not<TAppContext extends AppContext, TCallContext extends CallContext> extends NestedSecurityCheck<TAppContext, TCallContext> {

	@SuppressWarnings("unchecked")
	public Not(SecurityCheck<TAppContext, TCallContext>... items) {
		super(items);

		Validate.equals(items.length, 1);
	}

	@Override
	public boolean pass(TAppContext appContext, TCallContext callContext) {
		return !getChildren().get(0).pass(appContext, callContext);
	}

	@Override
	public String getDescription() {
		return "not";
	}

}
