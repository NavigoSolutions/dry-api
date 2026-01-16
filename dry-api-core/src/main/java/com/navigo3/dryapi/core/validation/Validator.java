package com.navigo3.dryapi.core.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;

public abstract class Validator {
	private final ObjectPathsTree allowedPaths;
	private List<ValidationItem> items = new ArrayList<>();

	private final boolean strictPathExistenceChecking;
	private final Logger logger;

	protected Validator(ObjectPathsTree allowedPaths, boolean strictPathExistenceChecking, Optional<Logger> logger) {
		this.allowedPaths = allowedPaths;
		this.strictPathExistenceChecking = strictPathExistenceChecking;
		this.logger = logger.orElseGet(() -> LoggerFactory.getLogger(getClass()));

	}

	public void addItem(Severity severity, StructurePath path, String message) {
		addItem(severity, path, message, strictPathExistenceChecking);
	}

	public void addItem(Severity severity, StructurePath path, String message, boolean checkPathExistence) {

		addItem(severity, path, message, checkPathExistence, Optional.empty());
	}

	public void addItem(Severity severity, StructurePath path, String message, boolean checkPathExistence,
		JsonNode extData) {

		addItem(severity, path, message, checkPathExistence, Optional.of(extData));
	}

	private void addItem(Severity severity, StructurePath path, String message, boolean checkPathExistence,
		Optional<JsonNode> extData) {

		if (checkPathExistence) {
			allowedPaths.checkPathExistence(path, err -> {
				if (strictPathExistenceChecking) {
					throw new RuntimeException(err);
				} else {
					this.logger.error(err);
				}
			});
		}

		items.add(
			ImmutableValidationItem.builder().severity(severity).path(path).message(message).extData(extData).build()
		);
	}

	public void addAll(Collection<ValidationItem> coll) {
		items.addAll(coll);
	}

	public ValidationData build() {
		return ImmutableValidationData.builder().items(items).build();
	}

	public ObjectPathsTree getAllowedPaths() {
		return allowedPaths;
	}

	public int getItemsCount() {
		return items.size();
	}

}