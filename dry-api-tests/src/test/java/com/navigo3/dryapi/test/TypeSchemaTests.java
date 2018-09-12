package com.navigo3.dryapi.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;

public class TypeSchemaTests {

	private static TypeSchema schema;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		schema = TypeSchema.build(new TypeReference<TopAddressInput>(){});
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		schema = null;
	}
	
	@Test
	public void validTwoFieldsPath() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress").addField("country"));
	}
	
	@Test
	public void validFieldIndexFieldPath() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress").addField("lowAddresses").addIndex().addField("number"));
	}
	
	@Test
	public void validFieldKeyFieldPath() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress").addField("stringCodes").addKey());
	}
	
	@Test(expected=Throwable.class)
	public void incompletePath() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress"));
	}
}
