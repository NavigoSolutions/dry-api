package com.navigo3.dryapi.test.core;

import java.time.LocalDateTime;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navigo3.dryapi.core.exec.json.JsonPathsTreeBuilder;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.sample.defs.philosophy.ImmutableLowAddressInput;
import com.navigo3.dryapi.sample.defs.philosophy.ImmutableMiddleAddressInput;
import com.navigo3.dryapi.sample.defs.philosophy.ImmutableTopAddressInput;

public class ObjectPathsTreeTest {
	private static ObjectPathsTree objectPathsTree;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		ImmutableLowAddressInput.Builder lowAddressBuilder = ImmutableLowAddressInput
			.builder()
			.city("Brno")
			.number(1245)
			.street("Charvatska");
		
		ImmutableMiddleAddressInput.Builder middleAddressBuilder = ImmutableMiddleAddressInput
			.builder()
			.continent("Europe")
			.country("Czech republic")
			.currentGalacticDateTime(LocalDateTime.now())
			.putCronicles(LocalDateTime.now().minusYears(1), "year ago")
			.putCronicles(LocalDateTime.now().minusYears(2), "two years ago")
			.putStringCodes("xxx", 123)
			.putStringCodes("yyy", 745)
			.mainAddress(lowAddressBuilder.build())
			.addLowAddresses(lowAddressBuilder.build())
			.addLowAddresses(lowAddressBuilder.build());
		
		ImmutableTopAddressInput.Builder topAddressBuilder = ImmutableTopAddressInput
			.builder()
			.corner("left")
			.galaxy("Milky way")
			.planet("Earth")
			.middleAddress(middleAddressBuilder.build());
		

		ObjectMapper objectMapper = JsonUtils.createMapper();
		
		String json = objectMapper.writeValueAsString(topAddressBuilder.build());
		
		JsonNode tree = objectMapper.readTree(json);
		
		objectPathsTree = JsonPathsTreeBuilder.parse(tree);
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
