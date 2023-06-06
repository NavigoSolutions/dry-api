package com.navigo3.dryapi.sample.defs.download;

import com.navigo3.dryapi.core.def.IOTypeReference;
import com.navigo3.dryapi.core.def.MethodInterface;
import com.navigo3.dryapi.predefined.params.DownloadParam;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;

public class DownloadPersonEndpoint implements MethodInterface<Person, DownloadParam> {

	@Override
	public String getQualifiedName() {
		return "donwload/person";
	}

	@Override
	public String getDescription() {
		return "Donwload name and surname of person as file";
	}

	@Override
	public IOTypeReference<Person> getInputType() {
		return new IOTypeReference<Person>() {
		};
	}

	@Override
	public IOTypeReference<DownloadParam> getOutputType() {
		return new IOTypeReference<DownloadParam>() {
		};
	}
}
