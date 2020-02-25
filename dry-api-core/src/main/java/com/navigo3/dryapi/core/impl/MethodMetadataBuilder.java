package com.navigo3.dryapi.core.impl;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.util.Validate;

public class MethodMetadataBuilder<TAppContext extends AppContext, TCallContext extends CallContext> {
	private final ImmutableMethodMetadata.Builder<TAppContext, TCallContext> builder = ImmutableMethodMetadata.builder();
	
	public MethodMetadata<TAppContext, TCallContext> build() {
		return builder.build();
	}
	
	/**
	 * Add flag used in documentation.
	 * @param flag
	 */
	public void addFlag(String flag) {
		Validate.notBlank(flag);
		
		builder.addFlags(flag);
	}
	
	/**
	 * Disable listing allowed fields in input/output. Can be used for reducing message size of huge lists.
	 */
	public void disableAllowedFields() {
		builder.disableAllowedFields(true);
	}
}
