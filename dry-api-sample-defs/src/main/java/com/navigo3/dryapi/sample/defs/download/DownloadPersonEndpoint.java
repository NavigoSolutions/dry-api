package com.navigo3.dryapi.sample.defs.download;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.predefined.params.DownloadParam;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;

public class DownloadPersonEndpoint extends MethodDefinition<Person, DownloadParam> {

	@Override
	public String getQualifiedName() {
		return "donwload/person";
	}

	@Override
	public String getDescription() {
		return "Donwload name and surname of person as file";
	}

	@Override
	public TypeReference<Person> getInputType() {
		return new TypeReference<Person>(){};
	}

	@Override
	public TypeReference<DownloadParam> getOutputType() {
		return new TypeReference<DownloadParam>(){};
	}
}
