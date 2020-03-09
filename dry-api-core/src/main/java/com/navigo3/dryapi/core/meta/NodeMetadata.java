package com.navigo3.dryapi.core.meta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.util.Validate;

@Value.Immutable
public interface NodeMetadata {
	public enum ContainerType {
		LIST,
		MAP,
		OPTIONAL
	}
	
	public enum ValueType {
		//scalar types
		NUMBER,
		STRING,
		BOOL,
		ENUMERABLE,
		DATE,
		TIME,
		DATETIME,
		
		//composed types
		OBJECT,
		
		//this node is recursive
		RECURSIVE
	}
	
	//value data
	Optional<ValueType> getValueType();
	List<String> getEnumItems();
	
	//container data
	Optional<ContainerType> getContainerType();
	Optional<ValueType> getKeyType();
	Optional<NodeMetadata> getItemType();
	
	//fields
	Map<String, NodeMetadata> getFields();
	
	@Value.Check
	default void check() {
		Validate.allOrNone(getContainerType(), getItemType());
		Validate.onePresent(getContainerType(), getValueType());
		
		if (getContainerType().isPresent() && getContainerType().get()==ContainerType.MAP) {
			Validate.isPresent(getKeyType());
		} else {
			Validate.notPresent(getKeyType());
		}
		
		getValueType().ifPresent(type->{
			if (type!=ValueType.ENUMERABLE) {
				Validate.isEmpty(getEnumItems());
			}
			
			if (type!=ValueType.OBJECT) {
				Validate.isEmpty(getFields());
			}
		});
	}
}