package com.navigo3.dryapi.test.core;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.util.JsonAccessor;
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;

public class JsonAccessorTest {
	private JsonNode rootNode;

	@Before
	public void setUp() throws Exception {
		rootNode = JsonUtils.createMapper().valueToTree(TopAddressInput.createSampleData());
	}
	
	@Test
	public void testReadCorrectPaths() {
		assertEquals("Earth", JsonAccessor.getNodeAt(rootNode, StructurePath.key("planet")).textValue());
		assertEquals("Czech republic", JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("country")).textValue());
		assertEquals(1245, JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0).addKey("number")).intValue());
	}
	
	@Test(expected=Exception.class)
	public void testReadWrongPath1() {
		assertEquals("Earth", JsonAccessor.getNodeAt(rootNode, StructurePath.key("hujer")).textValue());
	}
	
	@Test(expected=Exception.class)
	public void testReadWrongPath2() {
		assertEquals("Czech republic", JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddressExtraWrong").addKey("country")).textValue());
	}
	
	@Test(expected=Exception.class)
	public void testReadWrongPath3() {
		assertEquals(1245, JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(999).addKey("number")).intValue());
	}
	
	@Test
	public void testUpdateExistingFieldValue() {
		assertEquals("Czech republic", JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("country")).textValue());

		JsonAccessor.setNodeAt(rootNode, StructurePath.key("middleAddress").addKey("country"), TextNode.valueOf("Czechia"));

		assertEquals("Czechia", JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("country")).textValue());
		assertEquals(1245, JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0).addKey("number")).intValue());
	}
	
	@Test
	public void testUpdateExistingArrayValue() {
		assertEquals(1245, JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0).addKey("number")).intValue());

		JsonAccessor.setNodeAt(rootNode, StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0), TextNode.valueOf("Nonsense"));

		assertEquals("Nonsense", JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0)).textValue());
		assertEquals(1245, JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(1).addKey("number")).intValue());
	}
}
