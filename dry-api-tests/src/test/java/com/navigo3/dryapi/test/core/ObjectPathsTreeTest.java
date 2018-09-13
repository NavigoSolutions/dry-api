package com.navigo3.dryapi.test.core;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.navigo3.dryapi.core.exec.json.JsonPathsTreeBuilder;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;

public class ObjectPathsTreeTest {
	private static ObjectPathsTree objectPathsTree;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		objectPathsTree = JsonPathsTreeBuilder.fromObject(TopAddressInput.createSampleData());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		objectPathsTree = null;
	}
	
	@Test
	public void testValidPaths() {		
		objectPathsTree.throwIfPathDoesNotExists(StructurePath.key("galaxy"));
		objectPathsTree.throwIfPathDoesNotExists(StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0).addKey("door"));
	}
	
	@Test(expected=Exception.class)
	public void testNotEndedKey() {
		objectPathsTree.throwIfPathDoesNotExists(StructurePath.key("middleAddress"));
	}
	
	@Test(expected=Exception.class)
	public void testNotEndedIndex() {
		objectPathsTree.throwIfPathDoesNotExists(StructurePath.key("middleAddress").addKey("lowAddresses"));
	}
}
