package com.navigo3.dryapi.test.core;

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
	
	@Test
	public void validFieldKeyFieldPathUnderscores() {
		schema.throwIfPathNotExists(TypePath.field("middle_address").addField("string_codes").addKey());
	}
	
	@Test(expected=Throwable.class)
	public void incompletePath() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress"));
	}
	
	@Test
	public void validDatetimeField() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress").addField("currentGalacticDateTime"));
	}
	
	@Test
	public void validDatetimeKey() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress").addField("cronicles").addKey());
	}
	
	@Test
	public void validOptionalFieldInTheMiddle() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress").addField("mainAddress").addField("street"));
	}
	
	@Test
	public void validWithKeepRecursivelyOnField() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress").andRecursively());
	}
	
	@Test
	public void validWithKeepRecursivelyOnList() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress").addField("lowAddresses").andRecursively());
	}
	
	@Test
	public void validWithKeepRecursivelyOnMap() {
		schema.throwIfPathNotExists(TypePath.field("middleAddress").addField("cronicles").andRecursively());
	}
}
