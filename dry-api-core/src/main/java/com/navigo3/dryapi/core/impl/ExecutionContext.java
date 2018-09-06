package com.navigo3.dryapi.core.impl;

import java.util.Optional;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.util.Validate;

public class ExecutionContext<TContext extends AppContext, TCallContext extends CallContext> implements PublicExecutionContext<TContext, TCallContext> {

	private TContext appContext;
	private Optional<TCallContext> callContext = Optional.empty();
	private ObjectPathsTree inputPathsTree;
	private Optional<ObjectPathsTree> outputPathsTree = Optional.empty();
	
	public ExecutionContext(TContext appContext, ObjectPathsTree inputPathsTree) {
		Validate.notNull(appContext);
		Validate.notNull(inputPathsTree);
		
		this.appContext = appContext;
		this.inputPathsTree = inputPathsTree;
	}
	
	public void setCallContext(TCallContext callContext) {
		Validate.notNull(callContext);
		Validate.isFalse(this.callContext.isPresent());
		
		this.callContext = Optional.of(callContext);
	}
	
	public void setOutputPathsTree(ObjectPathsTree outputPathsTree) {
		Validate.notNull(outputPathsTree);
		Validate.isFalse(this.outputPathsTree.isPresent());
		
		this.outputPathsTree = Optional.of(outputPathsTree);
	}

	@Override
	public TContext getAppContext() {
		return appContext;
	}

	@Override
	public Optional<TCallContext> getCallContext() {
		return callContext ;
	}

	public ObjectPathsTree getInputPathsTree() {
		return inputPathsTree;
	}

	public Optional<ObjectPathsTree> getOutputPathsTree() {
		return outputPathsTree;
	}
}
