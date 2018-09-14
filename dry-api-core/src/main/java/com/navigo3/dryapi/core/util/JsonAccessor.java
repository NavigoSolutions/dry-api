package com.navigo3.dryapi.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.path.StructurePathItem;
import com.navigo3.dryapi.core.path.StructureSelectorType;

public class JsonAccessor {

	public static JsonNode getNodeAt(JsonNode node, StructurePath path) {
		if (!path.getItems().isEmpty()) {
			return getNodeAt(node, path, 0);
		} else {
			return node;
		}
	}
	
	public static void setNodeAt(JsonNode node, StructurePath path, JsonNode value) {
		if (!path.getItems().isEmpty()) {
			setNodeAt(node, path, value, 0);
		} else {
			Validate.equals(node.getNodeType(), value.getNodeType());
			
			if (node.isArray()) {
				((ArrayNode)node).removeAll();
				((ArrayNode)node).addAll((ArrayNode)value);
			} else if (node.isObject()) {
				((ObjectNode)node).removeAll();
				((ObjectNode)node).setAll((ObjectNode)value);
			} else {
				throw new RuntimeException("Unknown type "+node.getNodeType());
			}
		}
	}

	private static JsonNode getNodeAt(JsonNode node, StructurePath path, int index) {
		Validate.notNull(node);
		Validate.isValidIndex(path.getItems(), index);
		
		StructurePathItem item = path.getItems().get(index);
		
		if (item.getType()==StructureSelectorType.KEY) {
			Validate.isTrue(node.isObject());
			Validate.isTrue(node.has(item.getKey().get()));
			
			JsonNode subNode = node.get(item.getKey().get());
			
			if (index+1>=path.getItems().size()) {
				return subNode;
			} else {
				return getNodeAt(subNode, path, index+1);
			}
		} else if (item.getType()==StructureSelectorType.INDEX) {
			Validate.isTrue(node.isArray());
			Validate.isTrue(node.has(item.getIndex().get()));
			
			JsonNode subNode = node.get(item.getIndex().get());
			
			if (index+1>=path.getItems().size()) {
				return subNode;
			} else {
				return getNodeAt(subNode, path, index+1);
			}
			
		} else {
			throw new RuntimeException("Unknown type "+item.getType());
		}
	}
	
	private static void setNodeAt(JsonNode node, StructurePath path, JsonNode value, int index) {
		Validate.notNull(node);
		Validate.isValidIndex(path.getItems(), index);
		
		StructurePathItem item = path.getItems().get(index);
	
		if (item.getType()==StructureSelectorType.KEY) {
			Validate.isTrue(node.isObject());
			Validate.isTrue(node.has(item.getKey().get()));
			
			if (index+1>=path.getItems().size()) {
				((ObjectNode)node).set(item.getKey().get(), value);
			} else {
				JsonNode subNode = node.get(item.getKey().get());
				setNodeAt(subNode, path, value, index+1);
			}
		} else if (item.getType()==StructureSelectorType.INDEX) {
			Validate.isTrue(node.isArray());
			Validate.isTrue(node.has(item.getIndex().get()));
			
			if (index+1>=path.getItems().size()) {
				((ArrayNode)node).set(item.getIndex().get(), value);
			} else {
				JsonNode subNode = node.get(item.getIndex().get());
				setNodeAt(subNode, path, value, index+1);
			}
		} else {
			throw new RuntimeException("Unknown type "+item.getType());
		}
	}
}
