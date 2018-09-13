package com.navigo3.dryapi.core.exec.json;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.navigo3.dryapi.core.impl.MethodSecurity;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.field.FieldsSecurity;
import com.navigo3.dryapi.core.util.Consumer3;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.core.util.OptionalUtils;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.ValidationData;

public class JsonExecutor<TAppContext extends AppContext, TCallContext extends CallContext> {
	private static final Logger logger = LoggerFactory.getLogger(JsonExecutor.class);
	
	private final DryApi<TAppContext, TCallContext> api;

	public JsonExecutor(DryApi<TAppContext, TCallContext> api) {
		this.api = api;
	}
	
	public JsonBatchResponse execute(TAppContext context, JsonBatchRequest batch) {
		ImmutableJsonBatchResponse.Builder builder = ImmutableJsonBatchResponse.builder();
		
		batch.getRequests().forEach(request->{
			builder.addResponses(execute(context, request));
		});
		
		return builder.build();
	}

	private JsonResponse execute(TAppContext context, JsonRequest request) {
		
		ImmutableJsonResponse.Builder outputBuilder = ImmutableJsonResponse
			.builder()
			.requestUuid(request.getRequestUuid());
		
		findMethod(context, request, outputBuilder, def->{
			parseInputJson(context, request, outputBuilder, def, (input, objectMapper, executionContext)->{
				checkSecurity(context, request, outputBuilder, input, executionContext, objectMapper, def, (instance, callContext)->{
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
	private void findMethod(TAppContext context, JsonRequest request, Builder outputBuilder, Consumer<MethodDefinition> onSuccess) {
		Optional<MethodDefinition> optDef = api.lookupDefinition(request.getQualifiedName());
		
		OptionalUtils.ifPresentOrElse(optDef, onSuccess, ()->{
			outputBuilder
				.status(ResponseStatus.NOT_FOUND)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? StringUtils.subst("Method '{}' not found", request.getQualifiedName()) : null));
		});
	}

	@SuppressWarnings("rawtypes")
	private void parseInputJson(TAppContext context, JsonRequest request, Builder outputBuilder, MethodDefinition def, Consumer3<Object, ObjectMapper, ExecutionContext<TAppContext, TCallContext>> onSuccess) {
		ObjectMapper objectMapper = JsonUtils.createMapper();
		
		try {
			Object input = objectMapper.readValue(request.getInputJson(), def.getInputType());

			ExecutionContext<TAppContext, TCallContext> executionContext = new ExecutionContext<>(context);
			
			onSuccess.accept(input, objectMapper, executionContext);
		} catch (Throwable t) {
			logger.error("Error during parseInputJson", t);
			
			outputBuilder
				.status(ResponseStatus.MALFORMED_INPUT)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during parsing input", t) : null));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void checkSecurity(TAppContext context, JsonRequest request, Builder outputBuilder, Object input, ExecutionContext<TAppContext, TCallContext> executionContext,
			ObjectMapper objectMapper, MethodDefinition def, BiConsumer<MethodImplementation, CallContext> onSuccess) {
		try {
			Optional<Class<? extends MethodImplementation>> impl = api.lookupImplementationClass(request.getQualifiedName());
			
			Validate.isPresent(impl);
			
			MethodImplementation instance = ReflectionUtils.createInstance(impl.get());
			instance.initialize(api.lookupDefinition(request.getQualifiedName()).get(), executionContext);
			
			TCallContext callContext = (TCallContext) instance.prepareCallContext(input);
			
			Validate.notNull(callContext);
			
			Optional<MethodSecurity<TAppContext, TCallContext>> security = api.lookupSecurity(request.getQualifiedName());
			
			Validate.isPresent(security);
			
			executionContext.setCallContext(callContext);
			
			boolean pass = security.get().getAuthorization().pass(context, callContext);
						
			if (pass) {
				ObjectPathsTree inputPathsTree;
				
				if (security.get().getInputFieldsSecurity().isPresent()) {
					FieldsSecurity<TAppContext, TCallContext> fieldsSecurity = security.get().getInputFieldsSecurity().get();
					
					List<StructurePath> allowedPaths = fieldsSecurity.getAllowedPaths(executionContext.getAppContext(), callContext, def.getInputSchema());
					
					inputPathsTree = ObjectPathsTree.from(allowedPaths);
				} else {
					JsonNode inputNode = objectMapper.readValue(request.getInputJson(), JsonNode.class);
					inputPathsTree = JsonPathsTreeBuilder.parse(inputNode);
				}
				
				outputBuilder.allowedInputFields(inputPathsTree);

				onSuccess.accept(instance, callContext);
			} else {
				outputBuilder
					.status(ResponseStatus.NOT_AUTHORIZED)
					.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? "Not authorized" : null));
			}
			
		} catch (Throwable t) {
			logger.error("Error during checkSecurity()", t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_SECURITY)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during execution", t) : null));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void clearProhibitedInputFields(TAppContext context, Builder outputBuilder, Object input, MethodImplementation instance, 
			ObjectMapper objectMapper, CallContext callContext, Runnable block) {
		try {
			block.run();
		} catch (Throwable t) {
			logger.error("Error during clearProhibitedInputFields()", t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_CLEARING_INPUT)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during cleaning prohibited input fields", t) : null));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validate(TAppContext context, JsonRequest request, Builder outputBuilder, Object input, MethodImplementation instance, Runnable onSuccess) {
		try {
			ValidationData validationResult = instance.securedValidate(input);
	
			Validate.notNull(validationResult);
			
			outputBuilder
				.validation(validationResult);
			
			if (request.getRequestType()==RequestType.VALIDATE) {
				outputBuilder
					.status(ResponseStatus.SUCCESS);
			} else if (request.getRequestType()==RequestType.EXECUTE) {
				if (!validationResult.getOverallSuccess()) {
					outputBuilder
						.status(ResponseStatus.INVALID_INPUT)
						.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? "Input validation problem" : null));
				} else {
					onSuccess.run();
				}
			} else {
				throw new RuntimeException(StringUtils.subst("Unknown type: {}", request.getRequestType()));
			}			
		} catch (Throwable t) {
			logger.error("Error during validate()", t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_VALIDATION)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during validation", t) : null));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void execute(TAppContext context, JsonRequest request, Builder outputBuilder, Object input, MethodImplementation instance, ObjectMapper objectMapper,
			Consumer<Object> onSuccess) {
		try {
			Object output = instance.execute(input);
			
			onSuccess.accept(output);
		} catch (Throwable t) {
			logger.error("Error during execute()", t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_EXECUTION)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during execution", t) : null));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void clearProhibitedOutputFields(TAppContext context, Builder outputBuilder, Object output,
			MethodImplementation instance, ObjectMapper objectMapper, CallContext callContext, ExecutionContext executionContext) {
		try {
			String originalOutputJson = objectMapper.writeValueAsString(output);
			
			JsonNode outputNode = objectMapper.readValue(originalOutputJson, JsonNode.class);

			ObjectPathsTree outputPathsTree = JsonPathsTreeBuilder.parse(outputNode);
			
			int todo;
			outputBuilder.allowedOutputFields(outputPathsTree);

			outputBuilder
				.status(ResponseStatus.SUCCESS)
				.outputJson(objectMapper.writeValueAsString(output));
		} catch (Throwable t) {
			logger.error("Error during clearProhibitedOutputFields()", t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_CLEARING_OUTPUT)
				.errorMessage(Optional.ofNullable(context.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during cleaning prohibited output fields", t) : null));
		}
	}
}
