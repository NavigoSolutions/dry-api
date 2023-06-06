package com.navigo3.dryapi.sample.impls.download;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodMetadataBuilder;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.predefined.params.DownloadParam;
import com.navigo3.dryapi.predefined.params.ImmutableDownloadParam;
import com.navigo3.dryapi.sample.defs.download.DownloadPersonEndpoint;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;

public class DownloadPersonImpl extends MethodImplementation<Person, DownloadParam, DownloadPersonEndpoint, TestAppContext, TestCallContext, TestValidator> {

	@Override
	public void fillClassMetadata(MethodMetadataBuilder<TestAppContext, TestCallContext> metadata) {
	}

	@Override
	public void fillClassSecurity(MethodSecurityBuilder<TestAppContext, TestCallContext> securityBuilder) {
		securityBuilder.authorization(new True<>());
	}

	@Override
	public TestCallContext prepareCallContext(Person input) {
		return new TestCallContext();
	}

	@Override
	public void validate(Person input, TestValidator validator) {
	}

	@Override
	public DownloadParam execute(Person input) {
		return ExceptionUtils.withRuntimeException(() -> {
			byte[] data = StringUtils.subst("{}, {}\n", input.getSurname(), input.getName())
				.getBytes(StandardCharsets.UTF_8);
			String dataBase64 = Base64.getEncoder().encodeToString(data);

			return ImmutableDownloadParam.builder()
				.mimeType("text/plain")
				.name("person.txt")
				.contentBase64(dataBase64)
				.build();
		});
	}

}
