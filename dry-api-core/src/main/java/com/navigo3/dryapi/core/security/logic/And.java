package com.navigo3.dryapi.core.security.logic;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.security.core.NestedSecurityCheck;

public class And<TAppContext extends AppContext, TCallContext extends CallContext> extends NestedSecurityCheck<AppContext, CallContext> {

	@Override
	public boolean pass(AppContext appContext, CallContext callContext) {
		return getItems().stream().allMatch(i->i.pass(appContext, callContext));
	}

}
