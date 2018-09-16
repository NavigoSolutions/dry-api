package com.navigo3.dryapi.predefined.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodSecurity;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.core.ParentSecurityCheck;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.validation.ImmutableValidationData;
import com.navigo3.dryapi.core.validation.ImmutableValidationItem;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;
import com.navigo3.dryapi.predefined.def.GetMethodMetadataEndpoint;
import com.navigo3.dryapi.predefined.def.GetMethodMetadataEndpoint.MethodFullDescription;
import com.navigo3.dryapi.predefined.def.GetMethodMetadataEndpoint.SecurityNode;
import com.navigo3.dryapi.predefined.def.ImmutableMethodFullDescription;
import com.navigo3.dryapi.predefined.def.ImmutableSecurityNode;
import com.navigo3.dryapi.predefined.params.QualifiedNameParam;

public abstract class GetMethodMetadataImpl<TAppContext extends AppContext, TCallContext extends CallContext> 
	extends MethodImplementation<QualifiedNameParam, MethodFullDescription, GetMethodMetadataEndpoint, TAppContext, TCallContext> {

	public abstract DryApi<TAppContext, TCallContext> getApi();
	
	@Override
	public Optional<ValidationData> validate(QualifiedNameParam input) {
		ImmutableValidationData.Builder builder = ImmutableValidationData.builder();
	
		if (!getApi().lookupDefinition(input.getQualifiedName()).isPresent()) {
			builder.addItems(ImmutableValidationItem
				.builder()
				.severity(Severity.ERROR)
				.path(StructurePath.key("qualifiedName"))
				.message("There is no such method defined")
				.build());
		}
		
		return Optional.of(builder.build());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public MethodFullDescription execute(QualifiedNameParam input) {
		MethodDefinition definition = getApi().lookupDefinition(input.getQualifiedName()).get();
		MethodSecurity<TAppContext,TCallContext> security = getApi().lookupSecurity(input.getQualifiedName()).get();
		
		ImmutableMethodFullDescription.Builder builder = ImmutableMethodFullDescription.builder();
		
		fillDefinition(builder, definition);
		
		fillSecurity(builder, security);
		
		return builder.build();
	}

	@SuppressWarnings("rawtypes")
	private void fillDefinition(ImmutableMethodFullDescription.Builder builder, MethodDefinition definition) {
		builder
			.qualifiedName(definition.getQualifiedName())
			.description(definition.getDescription())
			.inputTypeSchema(definition.getInputSchema())
			.outputTypeSchema(definition.getOutputSchema());
	}

	private void fillSecurity(ImmutableMethodFullDescription.Builder builder, MethodSecurity<TAppContext,TCallContext> security) {
		builder.authorization(createSecurityNode(security.getAuthorization()));
	}

	@SuppressWarnings({ "unchecked" })
	private SecurityNode createSecurityNode(SecurityCheck<TAppContext,TCallContext> securityCheck) {
		ImmutableSecurityNode.Builder builder = ImmutableSecurityNode.builder();
		
		builder.description(securityCheck.getDescription());
		
		if (securityCheck instanceof ParentSecurityCheck) {
			List<SecurityNode> subs = ((ParentSecurityCheck<TAppContext,TCallContext>)securityCheck)
				.getChildren()
				.stream()
				.map(this::createSecurityNode)
				.collect(Collectors.toList());
			
			builder.children(subs);
		}
		
		return builder.build();
	}
}
