package com.navigo3.dryapi.sample.defs.list;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;

public class ListPersonsEndpoint extends MethodDefinition<Map<String, List<Person>>, List<Person>> {

	@Override
	public String getQualifiedName() {
		return "list/persons";
	}

	@Override
	public String getDescription() {
		return "Test of direct map/list on params";
	}

	@Override
	public TypeReference<Map<String, List<Person>>> getInputType() {
		return new TypeReference<Map<String, List<Person>>>(){};
	}

	@Override
	public TypeReference<List<Person>> getOutputType() {
		return new TypeReference<List<Person>>(){};
	}

}
