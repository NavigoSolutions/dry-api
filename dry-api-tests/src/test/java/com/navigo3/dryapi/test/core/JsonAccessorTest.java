package com.navigo3.dryapi.test.core;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.util.DryApiConstants;
import com.navigo3.dryapi.core.util.JacksonUtils;
import com.navigo3.dryapi.core.util.JsonAccessor;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;

public class JsonAccessorTest {
	private JsonNode rootNode;

	@Before
	public void setUp() throws Exception {
		rootNode = JacksonUtils.createJsonMapper(DryApiConstants.DEFAULT_MAX_SERIALIZABLE_STRING_LENGTH)
			.valueToTree(TopAddressInput.createSampleData());
	}

	@Test
	public void testReadCorrectPaths() {
		assertEquals("Earth", JsonAccessor.getNodeAt(rootNode, StructurePath.key("planet")).textValue());
		assertEquals(
			"Czech republic",
			JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("country")).textValue()
		);
		assertEquals(
			1245,
			JsonAccessor.getNodeAt(
				rootNode,
				StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0).addKey("number")
			).intValue()
		);
	}

	@Test(expected = Exception.class)
	public void testReadWrongPath1() {
		assertEquals("Earth", JsonAccessor.getNodeAt(rootNode, StructurePath.key("hujer")).textValue());
	}

	@Test(expected = Exception.class)
	public void testReadWrongPath2() {
		assertEquals(
			"Czech republic",
			JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddressExtraWrong").addKey("country")).textValue()
		);
	}

	@Test(expected = Exception.class)
	public void testReadWrongPath3() {
		assertEquals(
			1245,
			JsonAccessor.getNodeAt(
				rootNode,
				StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(999).addKey("number")
			).intValue()
		);
	}

	@Test
	public void testUpdateExistingFieldValue() {
		assertEquals(
			"Czech republic",
			JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("country")).textValue()
		);

		JsonAccessor.setNodeAt(
			rootNode,
			StructurePath.key("middleAddress").addKey("country"),
			TextNode.valueOf("Czechia")
		);

		assertEquals(
			"Czechia",
			JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("country")).textValue()
		);
		assertEquals(
			1245,
			JsonAccessor.getNodeAt(
				rootNode,
				StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0).addKey("number")
			).intValue()
		);
	}

	@Test
	public void testUpdateExistingArrayValue() {
		assertEquals(
			1245,
			JsonAccessor.getNodeAt(
				rootNode,
				StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0).addKey("number")
			).intValue()
		);

		JsonAccessor.setNodeAt(
			rootNode,
			StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0),
			TextNode.valueOf("Nonsense")
		);

		assertEquals(
			"Nonsense",
			JsonAccessor.getNodeAt(rootNode, StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(0))
				.textValue()
		);
		assertEquals(
			1245,
			JsonAccessor.getNodeAt(
				rootNode,
				StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(1).addKey("number")
			).intValue()
		);
	}

	@Test
	public void testCleanup() throws Exception {
		ObjectPathsTree futureTree = ObjectPathsTree.from(
			Arrays.asList(
				StructurePath.key("corner"),
				StructurePath.key("galaxy"),
				StructurePath.key("middleAddress").addKey("continent"),
				StructurePath.key("middleAddress").addKey("cronicles").addKey("2016-01-22 10:15:42"),
				StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(1).addKey("city"),
				StructurePath.key("middleAddress").addKey("lowAddresses").addIndex(1).addKey("street")
			)
		);

		JsonAccessor.cleanMissingFields(futureTree, rootNode);

		String outputJson = JacksonUtils.createJsonMapper(DryApiConstants.DEFAULT_MAX_SERIALIZABLE_STRING_LENGTH)
			.writeValueAsString(rootNode);

		JSONAssert.assertEquals(
			outputJson,
			"{\"corner\":\"left\",\"galaxy\":\"Milky way\",\"middleAddress\":{\"continent\":\"Europe\",\"country\":null,\"cronicles\":{\"2017-01-22 10:15:42\":null,\"2016-01-22 10:15:42\":\"two years ago\"},\"currentGalacticDateTime\":null,\"lowAddresses\":[null,{\"city\":\"Brno\",\"door\":null,\"number\":null,\"state\":null,\"street\":\"Charvatska\"}],\"mainAddress\":null,\"stringCodes\":null},\"planet\":null}\n",
			true
		);
	}
}
