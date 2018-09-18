package com.navigo3.dryapi.core.security.field;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.util.Validate;

public class ObjectFieldsSecurityBuilder<TAppContext extends AppContext, TCallContext extends CallContext> {
	public static <TAppContext extends AppContext, TCallContext extends CallContext> Map<StructurePath, SecurityCheck<TAppContext, TCallContext>>
		build(ObjectPathsTree validPaths, Consumer<ObjectFieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		ObjectFieldsSecurityBuilder<TAppContext, TCallContext> builder = new ObjectFieldsSecurityBuilder<>(validPaths);
		
		block.accept(builder);
		
		return builder.build();
	}
	
	private Map<StructurePath, SecurityCheck<TAppContext, TCallContext>> securityPerField = new HashMap<>();
	private ObjectPathsTree validPaths;
	
	public ObjectFieldsSecurityBuilder(ObjectPathsTree validPaths) {
		this.validPaths = validPaths;
	}
	
	private Map<StructurePath, SecurityCheck<TAppContext, TCallContext>> build() {
		
		throwIfNotFullyCovered();
		
		return securityPerField;
	}
	
	private void throwIfNotFullyCovered() {
		System.err.println("NOT IMPLEMENTED!!!!");
	}

	public void add(StructurePath path, SecurityCheck<TAppContext, TCallContext> security) {
		Validate.notNull(path);
		Validate.notNull(security);
		Validate.keyNotContained(securityPerField, path);
		
		validPaths.throwIfPathDoesNotExists(path);

		securityPerField.put(path, security);
	}
}
