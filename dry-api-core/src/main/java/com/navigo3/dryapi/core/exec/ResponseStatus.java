package com.navigo3.dryapi.core.exec;

public enum ResponseStatus {
	success,
	notFound,
	notAuthorized,
	internalErrorOnSecurity,
	internalErrorOnValidation,
	internalErrorOnExecution,
	internalErrorOnClearingInput,
	internalErrorOnClearingOutput,
	invalidInput,
	malformedInput
}
