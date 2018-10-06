package com.navigo3.dryapi.core.exec.json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.navigo3.dryapi.core.meta.ImmutableObjectPathsTree;
import com.navigo3.dryapi.core.meta.ImmutableObjectPathsTreeNode;
import com.navigo3.dryapi.core.meta.ImmutableObjectPathsTreeNode.Builder;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.meta.ObjectPathsTreeNode;
import com.navigo3.dryapi.core.path.StructureSelectorType;
import com.navigo3.dryapi.core.util.JacksonUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

public class JsonPathsTreeBuilder {
	
	public static ObjectPathsTree fromObject(Object o) {
		ObjectMapper mapper = JacksonUtils.createJsonMapper();
		
		return fromTree(mapper.valueToTree(o));
	}
	
	public static ObjectPathsTree fromTree(JsonNode jsonNode) {
		List<ObjectPathsTreeNode> items = new ArrayList<>();
		
		fillContainer(items, jsonNode);
		
		return ImmutableObjectPathsTree.builder().items(items).build();
	}
	
	private static void fillContainer(List<ObjectPathsTreeNode> items, JsonNode jsonNode) {
		Validate.isTrue(jsonNode.isContainerNode());
		
		if (jsonNode.isArray()) {
			fillArray(items, (ArrayNode)jsonNode);
		} else if (jsonNode.isObject()) {
			fillObject(items, (ObjectNode)jsonNode);
		} else {
			throw new RuntimeException(StringUtils.subst("Unsupported node type {}", jsonNode.getNodeType().name()));
		}
	}

	private static void fillObject(List<ObjectPathsTreeNode> items, ObjectNode object) {
		Validate.notNull(items);
		Validate.notNull(object);
		Validate.isTrue(object.isObject());
		
		object.fieldNames().forEachRemaining(key->{
			Builder itemBuilder = ImmutableObjectPathsTreeNode
				.builder()
				.type(StructureSelectorType.KEY)
				.key(key);
		
			JsonNode subJsonNode = object.get(key);
			
			if (subJsonNode.isContainerNode()) {
				List<ObjectPathsTreeNode> subitems = new ArrayList<>();
				
				fillContainer(subitems, subJsonNode);
				
				items.add(itemBuilder.items(subitems).build());
			} else {
				items.add(itemBuilder.build());
			}
		});
	}

	private static void fillArray(List<ObjectPathsTreeNode> items, ArrayNode array) {
		Validate.notNull(items);
		Validate.notNull(array);
		Validate.isTrue(array.isArray());
		
		for (int i=0;i<array.size();++i) {
			Builder itemBuilder = ImmutableObjectPathsTreeNode
				.builder()
				.type(StructureSelectorType.INDEX)
				.index(i);
		
			JsonNode subJsonNode = array.get(i);
			
			if (subJsonNode.isContainerNode()) {
				List<ObjectPathsTreeNode> subitems = new ArrayList<>();
				
				fillContainer(subitems, subJsonNode);
				
				items.add(itemBuilder.items(subitems).build());
			} else {
				items.add(itemBuilder.build());
			}
		}
	}
}
