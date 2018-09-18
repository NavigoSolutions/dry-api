package com.navigo3.dryapi.test.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.exec.json.JsonPathsTreeBuilder;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.security.field.TypeFieldsSecurity;
import com.navigo3.dryapi.core.security.field.TypeFieldsSecurityBuilder;
import com.navigo3.dryapi.core.security.logic.False;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;

public class TypeFieldsSecurityTest {
	private static TestAppContext appContext;
	private static TestCallContext callContext;
	private static Person data;
	private static TypeSchema schema;
	private static ObjectPathsTree pathsTree;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		appContext = new TestAppContext(true);
		callContext = new TestCallContext();
		
		data = Person.createSampleData();
		
		schema = TypeSchema.build(new TypeReference<Person>(){});
		
		pathsTree = JsonPathsTreeBuilder.fromObject(data);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}
	
	@Test
	public void test() {		
		TypeFieldsSecurity<TestAppContext, TestCallContext> typeFieldsSecurity = TypeFieldsSecurityBuilder.<TestAppContext, TestCallContext>build(schema, builder->{
			SecurityCheck<TestAppContext, TestCallContext> everyone = new True<>();
			SecurityCheck<TestAppContext, TestCallContext> nobody = new False<>();
			
			builder.add(TypePath.field("name"), everyone);
			builder.add(TypePath.field("surname"), everyone);
			builder.add(TypePath.field("age"), everyone);
			builder.add(TypePath.field("secretNumber"), nobody);
			builder.add(TypePath.field("colorsToFavoriteNumbers").addKey().addIndex(), everyone);
		});
		
		ObjectPathsTree clearanceTree = typeFieldsSecurity.getAllowedPaths(appContext, callContext, pathsTree);
		
		assertTrue(clearanceTree.keyExists(StructurePath.key("name")));
		assertTrue(clearanceTree.keyExists(StructurePath.key("surname")));
		assertTrue(clearanceTree.keyExists(StructurePath.key("age")));
		assertTrue(clearanceTree.keyExists(StructurePath.key("colorsToFavoriteNumbers").addKey("blue").addIndex(0)));
		assertTrue(clearanceTree.keyExists(StructurePath.key("colorsToFavoriteNumbers").addKey("blue").addIndex(1)));
		assertTrue(clearanceTree.keyExists(StructurePath.key("colorsToFavoriteNumbers").addKey("red").addIndex(0)));
		assertTrue(clearanceTree.keyExists(StructurePath.key("colorsToFavoriteNumbers").addKey("red").addIndex(1)));
		assertTrue(clearanceTree.keyExists(StructurePath.key("colorsToFavoriteNumbers").addKey("red").addIndex(2)));
		
		assertFalse(clearanceTree.keyExists(StructurePath.key("xxx")));
		assertFalse(clearanceTree.keyExists(StructurePath.key("colorsToFavoriteNumbers")));
		assertFalse(clearanceTree.keyExists(StructurePath.key("colorsToFavoriteNumbers").addKey("green").addIndex(0)));
		assertFalse(clearanceTree.keyExists(StructurePath.key("colorsToFavoriteNumbers").addKey("red").addIndex(3)));
	}
}