package com.navigo3.dryapi.core.exec.json;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import com.navigo3.dryapi.core.impl.MethodMetadata;
import com.navigo3.dryapi.core.impl.MethodMetadataBuilder;
import com.navigo3.dryapi.core.impl.MethodSecurity;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.field.ObjectFieldsSecurity;
import com.navigo3.dryapi.core.util.Consumer3;
import com.navigo3.dryapi.core.util.Consumer4;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.Function3;
import com.navigo3.dryapi.core.util.JsonAccessor;
import com.navigo3.dryapi.core.util.OptionalUtils;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.core.validation.Validator;

public class JsonExecutor<TAppContext extends AppContext, TCallContext extends CallContext, TValidator extends Validator> {
	private static final Logger logger = LoggerFactory.getLogger(JsonExecutor.class);
	
	private final DryApi<TAppContext, TCallContext, TValidator> api;
	
	private final Function3<TAppContext, TCallContext, ObjectPathsTree, TValidator> validatorProvider;

	private Consumer3<String, Duration, TAppContext> statsConsumer;

	public JsonExecutor(DryApi<TAppContext, TCallContext, TValidator> api, 
			Function3<TAppContext, TCallContext, ObjectPathsTree, TValidator> validatorProvider,
			Consumer3<String, Duration, TAppContext> statsConsumer) {
		Validate.notNull(api);
		Validate.notNull(validatorProvider);
		Validate.notNull(statsConsumer);
		
		this.api = api;
		this.validatorProvider = validatorProvider;
		this.statsConsumer = statsConsumer;
	}
	
	public JsonBatchResponse execute(TAppContext appContext, JsonBatchRequest batch, ObjectMapper objectMapper) {	
		ImmutableJsonBatchResponse.Builder builder = ImmutableJsonBatchResponse.builder();
		
		Map<String, JsonNode> previousResults = new HashMap<>();
		
		AtomicBoolean skipRest = new AtomicBoolean(false);
		
		AtomicReference<JsonBatchResponse> result = new AtomicReference<>();
		
		appContext.transaction(()->{
			batch.getRequests().forEach(request->{
				long startedAt = System.currentTimeMillis();
				
				JsonResponse resp;
						
				if (!skipRest.get()) {
					logger.debug("Executing method={}, uuid={}", request.getQualifiedName(), request.getRequestUuid());
					
					resp = executeRequest(appContext, request, objectMapper, (uuid, path)->{
						try {
							Validate.keyContained(previousResults, uuid, "");
			
							return JsonAccessor.getNodeAt(previousResults.get(uuid), path);
						} catch (Throwable t) {
							throw new RuntimeException(StringUtils.subst("Error during try to mapping value on path '{}' from request '{}'", path.toDebug(), uuid), t);
						}
					});
					
					if (resp.getStatus()!=ResponseStatus.SUCCESS) {
						skipRest.set(true);
					}
					
					if (resp.getRequestType()==RequestType.EXECUTE) {
						if (resp.getStatus()==ResponseStatus.SUCCESS) {
							if (resp.getOutput().isPresent()) {
								Validate.keyNotContained(previousResults, resp.getRequestUuid());
								previousResults.put(resp.getRequestUuid(), resp.getOutput().get());
							}
						}
					}
				} else {
					logger.debug("Skipping due to previous errors method={} uuid={}", request.getQualifiedName(), request.getRequestUuid());
					
					resp = ImmutableJsonResponse
						.builder()
						.qualifiedName(request.getQualifiedName())
						.requestType(request.getRequestType())
						.requestUuid(request.getRequestUuid())
						.status(ResponseStatus.NOT_PROCESSED_DUE_TO_PREVIOUS_ERRORS)
						.build();
				}
							
				builder.addResponses(resp);
				
				if (api.lookupDefinition(request.getQualifiedName()).isPresent()) {
					statsConsumer.accept(request.getQualifiedName(), Duration.ofMillis(System.currentTimeMillis()-startedAt), appContext);
				}
			});
			
			result.set(builder.build());
			
			return result.get().getOverallSuccess();
		});
				
		return result.get();
	}

	@SuppressWarnings("unchecked")
	private JsonResponse executeRequest(TAppContext appContext, JsonRequest request, ObjectMapper objectMapper,
			BiFunction<String, StructurePath, JsonNode> getPreviousOutput) {
		
		ImmutableJsonResponse.Builder outputBuilder = ImmutableJsonResponse
			.builder()
			.requestUuid(request.getRequestUuid())
			.requestType(request.getRequestType())
			.qualifiedName(request.getQualifiedName());
		
		if (appContext.getAllowedQualifiedNames().isPresent()) {
			if (!appContext.getAllowedQualifiedNames().get().contains(request.getQualifiedName())) {
				outputBuilder
					.status(ResponseStatus.METHOD_NOT_ALLOWED)
					.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? StringUtils.subst("Calling method '{}' not allowed by settings", request.getQualifiedName()) : null));
					
				return outputBuilder.build();
			}
		}
		
		findMethod(appContext, request, outputBuilder, def->{
			parseInputJson(appContext, request, outputBuilder, def, objectMapper, getPreviousOutput, (rawInput, executionContext)->{
				checkSecurity(appContext, request, outputBuilder, rawInput, executionContext, def, (instance, callContext, security, securityPassed)->{
					
					MethodMetadataBuilder<TAppContext, TCallContext> metadataBuilder = new MethodMetadataBuilder<>();
					instance.fillClassMetadata(metadataBuilder);
					MethodMetadata<TAppContext, TCallContext> meta = metadataBuilder.build();
					
					clearProhibitedInputFields(appContext, outputBuilder, rawInput, instance, callContext, security, def, request, securityPassed, objectMapper, meta, (input, inputPathsTree)->{
						validate(appContext, request, outputBuilder, input, instance, inputPathsTree, callContext, ()->{
							execute(appContext, request, outputBuilder, input, instance, rawOutput->{
								clearProhibitedOutputFields(request, appContext, outputBuilder, rawOutput, instance, callContext, executionContext, def, objectMapper, security, meta);
							});
						});
					});
					
				});
			});
		});
		
		return outputBuilder.build();
	}

	@SuppressWarnings("rawtypes")
	private void findMethod(TAppContext appContext, JsonRequest request, Builder outputBuilder, Consumer<MethodDefinition> onSuccess) {
		Optional<MethodDefinition> optDef = api.lookupDefinition(request.getQualifiedName());
		
		OptionalUtils.ifPresentOrElse(optDef, onSuccess, ()->{
			outputBuilder
				.status(ResponseStatus.NOT_FOUND)
				.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? StringUtils.subst("Method '{}' not found", request.getQualifiedName()) : null));
		});
	}

	@SuppressWarnings("rawtypes")
	private void parseInputJson(TAppContext appContext, JsonRequest request, Builder outputBuilder, MethodDefinition def, ObjectMapper objectMapper,
			BiFunction<String, StructurePath, JsonNode> getPreviousOutput, BiConsumer<Object, ExecutionContext<TAppContext, TCallContext>> onSuccess) {
		try {
			request.getInputMappings().forEach(m->{
				try {
					logger.debug("Copying output of {} on path {} to current input on path {}", m.getFromUuid(), m.getFromPath().toDebug(), m.getToPath().toDebug());
					JsonNode repl = getPreviousOutput.apply(m.getFromUuid(), m.getFromPath());
				
					JsonAccessor.setNodeAt(request.getInput(), m.getToPath(), repl);
				} catch (Throwable t) {
					throw new RuntimeException(StringUtils.subst(
						"Error during try to mapping value on path '{}' from request '{}' to path '{}'",
						m.getFromPath().toDebug(),
						m.getFromUuid(),
						m.getToPath().toDebug()
					), t);
				}
			});

			Object rawInput = objectMapper.convertValue(request.getInput(), def.getInputType());

			ExecutionContext<TAppContext, TCallContext> executionContext = new ExecutionContext<>(appContext);
			
			onSuccess.accept(rawInput, executionContext);
		} catch (Throwable t) {
			logger.error("Error during parseInputJson method={} uuid={}", request.getQualifiedName(), request.getRequestUuid(), t);
			
			appContext.reportException(t);
			
			outputBuilder
				.status(ResponseStatus.MALFORMED_INPUT)
				.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during parsing input", t) : null));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void checkSecurity(TAppContext appContext, JsonRequest request, Builder outputBuilder, Object rawInput, ExecutionContext<TAppContext, TCallContext> executionContext,
			MethodDefinition def, Consumer4<MethodImplementation, TCallContext, MethodSecurity<TAppContext, TCallContext>, Boolean> onSuccess) {
		try {
			Optional<Class<? extends MethodImplementation>> impl = api.lookupImplementationClass(request.getQualifiedName());
			
			Validate.isPresent(impl);
			
			MethodImplementation instance = ReflectionUtils.createInstance(impl.get());
			instance.initialize(api.lookupDefinition(request.getQualifiedName()).get(), executionContext);
			
			TCallContext callContext = (TCallContext) instance.prepareCallContext(rawInput);
			
			Validate.notNull(callContext);
			
			Optional<MethodSecurity<TAppContext, TCallContext>> security = api.lookupSecurity(request.getQualifiedName());
			
			Validate.isPresent(security);
			
			instance.setSecurity(security.get());
			
			executionContext.setCallContext(callContext);
			
			boolean pass = security.get().getAuthorization().pass(appContext, callContext);
						
			if (pass || request.getRequestType()==RequestType.INPUT_FIELDS_SECURITY) {
				onSuccess.accept(instance, callContext, security.get(), pass);
			} else {
				logger.info("Not authorized checkSecurity()");
				
				outputBuilder
					.status(ResponseStatus.NOT_AUTHORIZED)
					.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? "Not authorized" : null));
			}
			
		} catch (Throwable t) {
			logger.error("Error during checkSecurity() method={} uuid={}", request.getQualifiedName(), request.getRequestUuid(), t);
			
			appContext.reportException(t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_SECURITY)
				.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during execution", t) : null));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void clearProhibitedInputFields(TAppContext appContext, Builder outputBuilder, Object rawInput, MethodImplementation instance, 
			TCallContext callContext, MethodSecurity<TAppContext, TCallContext> security, MethodDefinition def, 
			JsonRequest request, boolean securityPassed, ObjectMapper objectMapper, MethodMetadata<TAppContext, TCallContext> meta,
			BiConsumer<Object, ObjectPathsTree> block) {
		try {
			ObjectPathsTree fullPathsTree = JsonPathsTreeBuilder.fromObject(rawInput);
			
			ObjectPathsTree inputPathsTree;
			boolean doCleaning = true;
			
			if (security.getInputFieldsTypeSecurity().isPresent()) {
				inputPathsTree = security.getInputFieldsTypeSecurity().get().getAllowedPaths(appContext, callContext, fullPathsTree);
			} else if (security.getInputFieldsObjectSecurity().isPresent()) {
				ObjectFieldsSecurity<TAppContext, TCallContext> dynamicFieldsSecurity = new ObjectFieldsSecurity<TAppContext, TCallContext>(security.getInputFieldsObjectSecurity().get());
				
				inputPathsTree = dynamicFieldsSecurity.getAllowedPaths(appContext, callContext, fullPathsTree);
			} else {
				inputPathsTree = fullPathsTree;
				doCleaning = false;
			}
			
			if (!securityPassed) {
				Validate.isEmpty(inputPathsTree.getItems(),
					StringUtils.subst(
						"If this request did not passed through security check, there should be no allowed fields! Namely:\n{}", 
						inputPathsTree.toPaths().stream().map(p->"\t"+p.toDebug()).collect(Collectors.joining("\n"))
					)
				);
			}
			
			if (!meta.getDisableAllowedFields()) {
				outputBuilder.allowedInputFields(inputPathsTree);
			}
			
			if (request.getRequestType()!=RequestType.INPUT_FIELDS_SECURITY) {
				Object input;
				
				if (doCleaning || !request.getInputMappings().isEmpty()) {
					JsonNode inputNode = objectMapper.valueToTree(rawInput);
					
					JsonAccessor.cleanMissingFields(inputPathsTree, inputNode);
					
					input = objectMapper.convertValue(inputNode, def.getInputType());
				} else {
					input = rawInput;
				}
				
				block.accept(input, inputPathsTree);
			} else {
				outputBuilder
					.status(ResponseStatus.SUCCESS);
			}
		} catch (Throwable t) {
			logger.error("Error during clearProhibitedInputFields() method={} uuid={}", request.getQualifiedName(), request.getRequestUuid(), t);
			
			appContext.reportException(t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_CLEARING_INPUT)
				.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during cleaning prohibited input fields", t) : null));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validate(TAppContext appContext, JsonRequest request, Builder outputBuilder, Object input, MethodImplementation instance, 
			ObjectPathsTree inputPathsTree, TCallContext callContext, Runnable onSuccess) {
		try {
			TValidator validator = validatorProvider.apply(appContext, callContext, inputPathsTree);
			
			instance.validate(input, validator);
			
			ValidationData validationResult = validator.build();
	
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
						.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? "Input validation problem" : null));
				} else {
					onSuccess.run();
				}
			} else {
				throw new RuntimeException(StringUtils.subst("Unknown type: {}", request.getRequestType()));
			}			
		} catch (Throwable t) {
			logger.error("Error during validate() method={} uuid={}", request.getQualifiedName(), request.getRequestUuid(), t);
			
			appContext.reportException(t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_VALIDATION)
				.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during validation", t) : null));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void execute(TAppContext appContext, JsonRequest request, Builder outputBuilder, Object input, MethodImplementation instance,
			Consumer<Object> onSuccess) {
		try {
			Object output = instance.execute(input);
			
			onSuccess.accept(output);
		} catch (Throwable t) {
			logger.error("Error during execute() method={} uuid={}", request.getQualifiedName(), request.getRequestUuid(), t);
			
			appContext.reportException(t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_EXECUTION)
				.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during execution", t) : null));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void clearProhibitedOutputFields(JsonRequest request, TAppContext appContext, Builder outputBuilder, Object rawOutput,
			MethodImplementation instance, TCallContext callContext, ExecutionContext executionContext,
			MethodDefinition def, ObjectMapper objectMapper, MethodSecurity<TAppContext, TCallContext> security, 
			MethodMetadata<TAppContext, TCallContext> meta) {
		try {
			ObjectPathsTree fullPathsTree = JsonPathsTreeBuilder.fromObject(rawOutput);
			
			ObjectPathsTree outputPathsTree;
			boolean doCleaning = true;
			
			if (security.getOutputFieldsTypeSecurity().isPresent()) {
				outputPathsTree = security.getOutputFieldsTypeSecurity().get().getAllowedPaths(appContext, callContext, fullPathsTree);
			} else if (security.getOutputFieldsObjectSecurity().isPresent()) {
				ObjectFieldsSecurity<TAppContext, TCallContext> dynamicFieldsSecurity = new ObjectFieldsSecurity<TAppContext, TCallContext>(security.getOutputFieldsObjectSecurity().get());
				
				outputPathsTree = dynamicFieldsSecurity.getAllowedPaths(appContext, callContext, fullPathsTree);
			} else {
				outputPathsTree = fullPathsTree;
				doCleaning = false;
			}
			
			if (!meta.getDisableAllowedFields()) {
				outputBuilder.allowedOutputFields(outputPathsTree);
			}
			
			Object output;
			
			if (doCleaning) {
				JsonNode outputNode = objectMapper.valueToTree(rawOutput);
				
				JsonAccessor.cleanMissingFields(outputPathsTree, outputNode);
				
				output = objectMapper.convertValue(outputNode, def.getOutputType());
			} else {
				output = rawOutput;
			}

			outputBuilder
				.status(ResponseStatus.SUCCESS)
				.output(objectMapper.valueToTree(output));
		} catch (Throwable t) {
			logger.error("Error during clearProhibitedOutputFields() method={} uuid={}", request.getQualifiedName(), request.getRequestUuid(), t);
			
			appContext.reportException(t);
			
			outputBuilder
				.status(ResponseStatus.INTERNAL_ERROR_ON_CLEARING_OUTPUT)
				.errorMessage(Optional.ofNullable(appContext.getIsDevelopmentInstance() ? ExceptionUtils.extractStacktrace("Internal error during cleaning prohibited output fields", t) : null));
		}
	}
}
