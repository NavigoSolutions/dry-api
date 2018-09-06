package com.navigo3.dryapi.core.exec.json;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.exec.ResponseStatus;
import com.navigo3.dryapi.core.exec.json.ImmutableJsonResponse.Builder;
import com.navigo3.dryapi.core.exec.json.JsonRequest.RequestType;
import com.navigo3.dryapi.core.impl.ExecutionContext;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.util.Consumer3;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.core.util.OptionalUtils;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.ImmutableValidationResult;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;
import com.navigo3.dryapi.core.validation.ValidationResult;

public class JsonExecutor<TContext extends AppContext, TCallContext extends CallContext> {

	private final DryApi<TContext, TCallContext> api;

	public JsonExecutor(DryApi<TContext, TCallContext> api) {
		this.api = api;
	}
	
	public JsonBatchResponse execute(TContext context, JsonBatchRequest batch) {
		ImmutableJsonBatchResponse.Builder builder = ImmutableJsonBatchResponse.builder();
		
		batch.getRequests().forEach(request->{
			builder.addResponses(execute(context, request));
		});
		
		return builder.build();
	}

	private JsonResponse execute(TContext context, JsonRequest request) {
		
		ImmutableJsonResponse.Builder outputBuilder = ImmutableJsonResponse
			.builder()
			.requestUuid(request.getRequestUuid());
		
		findMethod(context, request, outputBuilder, def->{
			parseInputJson(context, request, outputBuilder, def, (input, objectMapper, executionContext)->{
				checkSecurity(context, request, outputBuilder, input, executionContext, (instance, callContext)->{
					clearProhibitedInputFields(context, outputBuilder, input, instance, objectMapper, callContext, ()->{
						validate(context, request, outputBuilder, input, instance, ()->{
							execute(context, request, outputBuilder, input, instance, objectMapper, output->{
								clearProhibitedOutputFields(context, outputBuilder, output, instance, objectMapper, callContext, executionContext);
							});
						});
					});
					
				});
			});
		});
		
		return outputBuilder.build();
	}

	@SuppressWarnings("rawtypes")
	private void findMethod(TContext context, JsonRequest request, Builder outputBuilder, Consumer<MethodDefinition> onSuccess) {
		Optional<MethodDefinition> optDef = api.lookupDefinition(request.getQualifiedName());
		
		OptionalUtils.ifPresentOrElse(optDef, onSuccess, ()->{
			outputBuilder
				.status(ResponseStatus.notFound)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? StringUtils.subst("Method '{}' not found", request.getQualifiedName()) : null));
		});
	}

	@SuppressWarnings("rawtypes")
	private void parseInputJson(TContext context, JsonRequest request, Builder outputBuilder, MethodDefinition def, Consumer3<Object, ObjectMapper, ExecutionContext> onSuccess) {
		ObjectMapper objectMapper = JsonUtils.createMapper();
		
		try {
			JsonNode inputNode = objectMapper.readValue(request.getInputJson(), JsonNode.class);
			
			Object input = objectMapper.readValue(request.getInputJson(), def.getInputType());
			
			ObjectPathsTree inputPathsTree = JsonPathsTreeBuilder.parse(inputNode);
			
			ExecutionContext executionContext = new ExecutionContext<>(context, inputPathsTree);
			
			outputBuilder.allowedInputFields(inputPathsTree);
			
			onSuccess.accept(input, objectMapper, executionContext);
		} catch (Throwable t) {
			outputBuilder
				.status(ResponseStatus.malformedInput)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during parsing input", t) : null));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void checkSecurity(TContext context, JsonRequest request, Builder outputBuilder, Object input, ExecutionContext executionContext,
			BiConsumer<MethodImplementation, CallContext> onSuccess) {
		try {
			Optional<Class<? extends MethodImplementation>> impl = api.lookupImplementationClass(request.getQualifiedName());
			
			Validate.isPresent(impl);
			
			MethodImplementation instance = ReflectionUtils.createInstance(impl.get());
			instance.initialize(executionContext);
			
			CallContext callContext = instance.prepareCallContext(input);
			
			Validate.notNull(callContext);
			
			executionContext.setCallContext(callContext);
			
			boolean pass = instance.getAuthorization().pass(context, callContext);
						
			if (pass) {
				onSuccess.accept(instance, callContext);
			} else {
				outputBuilder
					.status(ResponseStatus.notAuthorized)
					.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? "Not authorized" : null));
			}
			
		} catch (Throwable t) {
			outputBuilder
				.status(ResponseStatus.internalErrorOnSecurity)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during execution", t) : null));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void clearProhibitedInputFields(TContext context, Builder outputBuilder, Object input, MethodImplementation instance, 
			ObjectMapper objectMapper, CallContext callContext, Runnable block) {
		try {
			block.run();
		} catch (Throwable t) {
			outputBuilder
				.status(ResponseStatus.internalErrorOnClearingInput)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during cleaning prohibited input fields", t) : null));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void validate(TContext context, JsonRequest request, Builder outputBuilder, Object input, MethodImplementation instance, Runnable onSuccess) {
		try {
			Optional<ValidationResult> validationResult = instance.validate(input);
			
			Validate.notNull(validationResult);
			
			outputBuilder
				.validation(validationResult.orElse(ImmutableValidationResult.builder().build()));
			
			if (request.getRequestType()==RequestType.validate) {
				outputBuilder
					.status(ResponseStatus.success);
			} else if (request.getRequestType()==RequestType.execute) {
				if (validationResult.isPresent() && validationResult.get().getItems().stream().anyMatch(i->i.getSeverity()==Severity.error)) {
					outputBuilder
						.status(ResponseStatus.invalidInput)
						.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? "Input validation problem" : null));
				} else {
					onSuccess.run();
				}
			} else {
				throw new RuntimeException(StringUtils.subst("Unknown type: {}", request.getRequestType()));
			}			
		} catch (Throwable t) {
			outputBuilder
				.status(ResponseStatus.internalErrorOnValidation)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during validation", t) : null));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void execute(TContext context, JsonRequest request, Builder outputBuilder, Object input, MethodImplementation instance, ObjectMapper objectMapper,
			Consumer<Object> onSuccess) {
		try {
			Object output = instance.execute(input);
			
			onSuccess.accept(output);
		} catch (Throwable t) {
			outputBuilder
				.status(ResponseStatus.internalErrorOnExecution)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during execution", t) : null));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void clearProhibitedOutputFields(TContext context, Builder outputBuilder, Object output,
			MethodImplementation instance, ObjectMapper objectMapper, CallContext callContext, ExecutionContext executionContext) {
		try {
			String originalOutputJson = objectMapper.writeValueAsString(output);
			
			JsonNode outputNode = objectMapper.readValue(originalOutputJson, JsonNode.class);

			ObjectPathsTree outputPathsTree = JsonPathsTreeBuilder.parse(outputNode);

			executionContext.setOutputPathsTree(outputPathsTree);
			
			outputBuilder.allowedOutputFields(outputPathsTree);
				
			outputBuilder
				.status(ResponseStatus.success)
				.outputJson(objectMapper.writeValueAsString(output));
		} catch (Throwable t) {
			outputBuilder
				.status(ResponseStatus.internalErrorOnClearingOutput)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during cleaning prohibited output fields", t) : null));
		}
	}
}
