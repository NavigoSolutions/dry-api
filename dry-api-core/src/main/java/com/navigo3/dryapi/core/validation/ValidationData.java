package com.navigo3.dryapi.core.validation;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;

@Value.Immutable
@JsonSerialize(as = ImmutableValidationData.class)
@JsonDeserialize(as = ImmutableValidationData.class)
@JsonIgnoreProperties(value={ "overallSuccess" }, allowGetters=true)
public interface ValidationData {
	List<ValidationItem> getItems();
	
	default boolean getOverallSuccess() {
		return getItems().stream().noneMatch(r->r.getSeverity()==Severity.ERROR);
	}
	
	default String toDebug() {
		if (getOverallSuccess()) {
			return "Everything is perfectly valid!";
		} else {
			StringBuilder res = new StringBuilder("Validation issues:\n");
	
			
			getItems().forEach(item->{
				res.append(StringUtils.subst("{}: {}\n", item.getPath().toDebug(), item.getMessage()));
			});
			
			return res.toString();
		}
	}

	default void printDebug() {
		System.out.println(toDebug());
	}
}
