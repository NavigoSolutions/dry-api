package com.navigo3.dryapi.core.security.logic;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.security.core.SecurityCheck;

public class Nobody<TAppContext extends AppContext, TCallContext extends CallContext> implements SecurityCheck<TAppContext, TCallContext> {
	
	@Override
	public boolean pass(TAppContext appContext, TCallContext callContext) {
		return false;
	}
	
}
