package com.navigo3.dryapi.core.security.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;

public abstract class NestedSecurityCheck<TAppContext extends AppContext, TCallContext extends CallContext> 
	implements SecurityCheck<TAppContext, TCallContext>, ParentSecurityCheck<TAppContext, TCallContext> {

	private final List<SecurityCheck<TAppContext, TCallContext>> items = new ArrayList<>();

	@SafeVarargs
	public NestedSecurityCheck(SecurityCheck<TAppContext, TCallContext>...items) {
		this.items.addAll(Arrays.asList(items));
	}

	public List<SecurityCheck<TAppContext, TCallContext>> getChildren() {
		return items;
	}
}
