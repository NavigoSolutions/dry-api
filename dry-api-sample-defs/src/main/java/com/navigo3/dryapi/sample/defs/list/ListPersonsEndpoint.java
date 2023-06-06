package com.navigo3.dryapi.sample.defs.list;

import java.util.List;
import java.util.Map;

import com.navigo3.dryapi.core.def.IOTypeReference;
import com.navigo3.dryapi.core.def.MethodInterface;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;

public class ListPersonsEndpoint implements MethodInterface<Map<String, List<Person>>, List<Person>> {

	@Override
	public String getQualifiedName() {
		return "list/persons";
	}

	@Override
	public String getDescription() {
		return "Test of direct map/list on params";
	}

	@Override
	public IOTypeReference<Map<String, List<Person>>> getInputType() {
		return new IOTypeReference<Map<String, List<Person>>>() {
		};
	}

	@Override
	public IOTypeReference<List<Person>> getOutputType() {
		return new IOTypeReference<List<Person>>() {
		};
	}

}
