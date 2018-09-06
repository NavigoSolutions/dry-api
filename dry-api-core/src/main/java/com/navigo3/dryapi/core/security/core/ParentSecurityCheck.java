package com.navigo3.dryapi.core.security.core;

import java.util.List;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;

public interface ParentSecurityCheck<TAppContext extends AppContext, TCallContext extends CallContext> {
	List<SecurityCheck<TAppContext, TCallContext>> getChildren();
}
