package com.navigo3.dryapi.core.security.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.meta.ObjectPathsTreeNode;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.path.StructureSelectorType;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.path.TypePathItem;
import com.navigo3.dryapi.core.path.TypeSelectorType;
import com.navigo3.dryapi.core.security.core.SecurityCheck;

public class TypeFieldsSecurity<TAppContext extends AppContext, TCallContext extends CallContext> {
	private Map<TypePath, SecurityCheck<TAppContext, TCallContext>> securityPerField;

	public TypeFieldsSecurity(Map<TypePath, SecurityCheck<TAppContext, TCallContext>> securityPerField) {
		this.securityPerField = securityPerField;
	}

	public ObjectPathsTree getAllowedPaths(TAppContext appContext, TCallContext callContext, ObjectPathsTree validPaths) {
		List<CacheEntry<TAppContext, TCallContext>> cache = new ArrayList<>();
		
		List<TypePath> allowedTypePaths = securityPerField
			.entrySet()
			.stream()
			.filter(e->{
				Optional<CacheEntry<TAppContext, TCallContext>> cacheEntry = cache
					.stream()
					.filter(c->c.getSecurityCheck()==e.getValue()) //really, compare objects by identity!
					.findFirst();
				
				if (cacheEntry.isPresent()) {
					return cacheEntry.get().getPassed();
				}
				
				boolean passed = e.getValue().pass(appContext, callContext);
				
				cache.add(ImmutableCacheEntry.<TAppContext, TCallContext>builder().securityCheck(e.getValue()).passed(passed).build());
				
				return passed;
			})
			.map(e->e.getKey())
			.collect(Collectors.toList());

		List<StructurePath> res = new ArrayList<>();
		
		validPaths.getItems().forEach(node->{
			addLeafsPath(res, node, 0, filterMatching(allowedTypePaths, node, 0), StructurePath.empty());
		});		
		
		return ObjectPathsTree.from(res);
	}

	private void addLeafsPath(List<StructurePath> res, ObjectPathsTreeNode node, int index, List<TypePath> allowedTypePaths, StructurePath path) {
		if (allowedTypePaths.isEmpty()) {
			return;
		}
		
		StructurePath currentPath;
		
		if (node.getType()==StructureSelectorType.INDEX) {
			currentPath = path.addIndex(node.getIndex().get());
		} else if (node.getType()==StructureSelectorType.KEY) {
			currentPath = path.addKey(node.getKey().get());
		} else {
			throw new RuntimeException("Unknown type "+node.getType());
		}
		
		if (!node.getItems().isPresent() || node.getItems().get().isEmpty()) {
			res.add(currentPath);
		}
		
		node.getItems().ifPresent(items->{
			items.forEach(item->{
				addLeafsPath(res, item, index+1, filterMatching(allowedTypePaths, item, index+1), currentPath);
			});
		});
	}

	private List<TypePath> filterMatching(List<TypePath> allowedTypePaths, ObjectPathsTreeNode node, int index) {
		return allowedTypePaths
			.stream()
			.filter(p->{
				if (p.getItems().size()>index) {
					TypePathItem item = p.getItems().get(index);
					
					if (item.getType()==TypeSelectorType.INDEX && node.getType()==StructureSelectorType.INDEX) {
						return true;
					} else if (item.getType()==TypeSelectorType.KEY && node.getType()==StructureSelectorType.KEY) {
						return true;
					} else if (item.getType()==TypeSelectorType.FIELD && node.getType()==StructureSelectorType.KEY) {
						if (item.getFieldName().get().equals(node.getKey().get())) {
							return true;
						}
					} else if (item.getType()==TypeSelectorType.KEEP_RECURSIVELY) {
						return true;
					}
					
					return false;
				} else {
					if (p.getItems().get(p.getItems().size()-1).getType()==TypeSelectorType.KEEP_RECURSIVELY) {
						return true;
					} else {
						return false;
					}
				}
			})
			.collect(Collectors.toList());
	}
}
