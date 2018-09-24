package com.navigo3.dryapi.core.security.core;

import java.util.Arrays;
import java.util.List;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.util.Validate;

public abstract class WrapperSecurityCheck<TAppContext extends AppContext, TCallContext extends CallContext> 
	implements SecurityCheck<TAppContext, TCallContext>, ParentSecurityCheck<TAppContext, TCallContext> {

	private List<SecurityCheck<TAppContext, TCallContext>> items;

	public WrapperSecurityCheck(SecurityCheck<TAppContext, TCallContext> item) {
		Validate.notNull(item);
		
		this.items = Arrays.asList(item);
	}
	
	@Override
	public boolean pass(TAppContext appContext, TCallContext callContext) {
		return items.get(0).pass(appContext, callContext);
	}

	@Override
	public List<SecurityCheck<TAppContext, TCallContext>> getChildren() {
		return items;
	}

}
