package com.navigo3.dryapi.core.security.core;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;

public interface SecurityCheck<TAppContext extends AppContext, TCallContext extends CallContext> {
	boolean pass(TAppContext appContext, TCallContext callContext);
	
	String getDescription();
}
