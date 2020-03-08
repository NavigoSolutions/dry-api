package com.navigo3.dryapi.core.meta;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.meta.ImmutableNodeMetadata.Builder;
import com.navigo3.dryapi.core.meta.NodeMetadata.ContainerType;
import com.navigo3.dryapi.core.meta.NodeMetadata.ValueType;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.path.TypePathItem;
import com.navigo3.dryapi.core.path.TypeSelectorType;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

/***
 * This class converts Java type into JSON structure metadata. When Java instance is converted into JSON by 
 * Jackson library, it should have structure described by this metadata.
 */
public class TypeSchema {
	private static final Logger logger = LoggerFactory.getLogger(TypeSchema.class);

	private NodeMetadata root;
	
	public static TypeSchema build(TypeReference<?> type) {
		TypeSchema res = new TypeSchema();
		
		res.parse(type);
		
		return res;
	}

	private TypeSchema() {
	}

	public NodeMetadata getRoot() {
		return root;
	}
	
	public void throwIfPathNotExists(TypePath path) {
		NodeMetadata actNode = root;
		StringBuilder fullPath = new StringBuilder();
		
		for (TypePathItem item : path.getItems()) {
			if (actNode!=root) {
				fullPath.append(" -> ");
			}
			
			fullPath.append(item.getDebug());
			
			if (item.getType()==TypeSelectorType.FIELD) {
				Validate.isPresent(actNode.getValueType(), "Expected value type at path {} ({})", fullPath, actNode);
				Validate.equals(actNode.getValueType().get(), ValueType.OBJECT, "Expected object at path {}", fullPath);
				Validate.keyContained(
					actNode.getFields(),
					item.getFieldName().get(),
					"Unknown field {}, possibilities are: {}",
					item.getFieldName().get(),
					actNode.getFields().keySet().stream().collect(Collectors.joining(","))
				);
				
				actNode = actNode.getFields().get(item.getFieldName().get());
			} else if (item.getType()==TypeSelectorType.INDEX) {
				Validate.isPresent(actNode.getContainerType(), "Expected container at path {}", fullPath);
				Validate.equals(actNode.getContainerType().get(), ContainerType.LIST, "Expected array at path {}", fullPath);
				
				actNode = actNode.getItemType().get();
			} else if (item.getType()==TypeSelectorType.KEY) {
				Validate.isPresent(actNode.getContainerType(), "Expected container at path {}", fullPath);
				Validate.equals(actNode.getContainerType().get(), ContainerType.MAP, "Expected map at path {}", fullPath);
				
				actNode = actNode.getItemType().get();
			} else if (item.getType()==TypeSelectorType.KEEP_RECURSIVELY) {
				return;
			} else {
				throw new RuntimeException("Unexpected type "+item.getType().name());
			}
			
			if (actNode.getContainerType().isPresent() && actNode.getContainerType().get()==ContainerType.OPTIONAL) {
				actNode = actNode.getItemType().get();
			}
		}
		
		if (actNode.getContainerType().isPresent() || actNode.getValueType().get()==ValueType.OBJECT) {
			throw new RuntimeException("This path does not end on value node!");
		}
	}
	
	public void debugPrint() {
		ExceptionUtils.withRuntimeException(()->{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os);
			
			debugPrint(ps);
			
			System.out.println(os.toString(StandardCharsets.UTF_8.name()));
		});
	}
	
	public void debugPrint(PrintStream out) {
		debugPrint(out, 0, "    ");
	}
	
	public void debugPrint(PrintStream out, int indentCount, String indentStr) {
		debugPrint(out, root, Optional.empty(), indentCount, indentStr);
	}
	
	private void debugPrint(PrintStream out, NodeMetadata node, Optional<String> name, int indentLevel, String indentStr) {
		out.print(StringUtils.repeat(indentLevel, indentStr));
		
		name.ifPresent(s->out.print(s+" : "));
		
		node.getContainerType().ifPresent(type->{
			if (type==ContainerType.LIST) {
				out.print("[]");
			} else if (type==ContainerType.MAP) {
				out.print("{"+node.getKeyType().get().name()+"}");
			} else if (type==ContainerType.OPTIONAL) {
				out.print("(?)");
			} else {
				throw new RuntimeException("Unsupported container type "+type.name());
			}
		});
		
		node.getValueType().ifPresent(t->{
			out.print(t.name());
			
			if (t==ValueType.ENUMERABLE) {
				out.print("["+node.getEnumItems().stream().collect(Collectors.joining(", "))+"]");
			}
		});
		
		out.println();
		
		node.getContainerType().ifPresent(type->{
			debugPrint(out, node.getItemType().get(), Optional.empty(), indentLevel+1, indentStr);
		});
		
		node.getFields().entrySet().stream().sorted(Comparator.comparing(e->e.getKey())).forEach(e->{
			debugPrint(out, e.getValue(), Optional.of(e.getKey()), indentLevel+1, indentStr);
		});
	}

	private void parse(TypeReference<?> type) {
		final Class<?> klass;

		HashMap<String, Type> templateParams = new HashMap<>();
		
		if (type.getType() instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType)type.getType();
			klass = (Class<?>)paramType.getRawType();

			for (int i=0;i<paramType.getActualTypeArguments().length;++i) {
				Type paramJavaType = paramType.getActualTypeArguments()[i];
				
				templateParams.put(klass.getTypeParameters()[i].getName(), paramJavaType);
			}
		} else {
			klass = (Class<?>)type.getType();
		}

		root = prepareNode(klass, type.getType().getTypeName(), templateParams, new HashSet<>());
	}

	private NodeMetadata prepareNode(Class<?> klass, String klassReal, HashMap<String, Type> templateParams, Set<Class<?>> alreadyVisited) {
//		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//		System.out.println(klass);
//		System.out.println(klassReal);
		
		if (alreadyVisited.contains(klass)) {
			return ImmutableNodeMetadata
				.builder()
				.valueType(ValueType.RECURSIVE)
				.build();
		}
		
		Builder builder = ImmutableNodeMetadata.builder();
		
		Optional<ValueType> optValueType = classToValueType(klass);
		
		optValueType.ifPresent(valueType->{
			builder.valueType(valueType);
			
			if (valueType==ValueType.ENUMERABLE) {
				builder.enumItems(Stream.of(klass.getEnumConstants()).map(o->((Enum<?>)o).name()).sorted().collect(Collectors.toList()));
			}
		});
		
		if (!optValueType.isPresent()) {
			if (Optional.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.OPTIONAL);

				List<String> params = ReflectionUtils.parseTemplateParams(klassReal);
				Validate.size(params, 1);
				
				ExceptionUtils.withRuntimeException(()->{
					String paramClassName = params.get(0).split("<")[0];
					
//					List<String> paramsSub = ReflectionUtils.parseTemplateParams(klassReal);
					
					builder.itemType(prepareNode(Class.forName(paramClassName), params.get(0), templateParams, alreadyVisited));
				});		
			} else if (Collection.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.LIST);
				
				List<String> params = ReflectionUtils.parseTemplateParams(klassReal);
				Validate.size(params, 1);
				
				ExceptionUtils.withRuntimeException(()->{
					String paramClassName = params.get(0).split("<")[0];
					
					builder.itemType(prepareNode(Class.forName(paramClassName), params.get(0), templateParams, alreadyVisited));
				});
			} else if (klass.isArray()) {
				builder.containerType(ContainerType.LIST);
				
				builder.itemType(prepareNode(klass.getComponentType(), klass.getComponentType().getName(), templateParams, alreadyVisited));
			} else if (Map.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.MAP);
				
				List<String> params = ReflectionUtils.parseTemplateParams(klassReal);
				Validate.size(params, 2);
				
				ExceptionUtils.withRuntimeException(()->{
					String paramClassNameK = params.get(0).split("<")[0];
					String paramClassNameV = params.get(1).split("<")[0];
					
					Optional<ValueType> keyType = classToValueType(Class.forName(paramClassNameK));
					
					if (!keyType.isPresent()) {
						logger.warn("Implausible map key type '{}'. Are you sure it has correct toString() method?", paramClassNameK);
					}
					
					builder.keyType(keyType.orElse(ValueType.OBJECT));
					builder.itemType(prepareNode(Class.forName(paramClassNameV), params.get(1), templateParams, alreadyVisited));
				});
			} else {
				builder.valueType(ValueType.OBJECT);
				
				alreadyVisited.add(klass);
				
				fillFields(builder, klass, templateParams, alreadyVisited);
				
				alreadyVisited.remove(klass);
			}
		}
		
		return builder.build();
	}
	
	private void fillFields(Builder builder, Class<?> klass, HashMap<String, Type> templateParams, Set<Class<?>> alreadyVisited) {
		Set<String> uniquenessNameCheck = new HashSet<>();
		
		for (Method method : klass.getMethods()) {
			//core packages
			if (method.getDeclaringClass().getName().startsWith("sun.") || method.getDeclaringClass().getName().startsWith("java.")) {
				continue;
			}
			
			//is not public
			if ((method.getModifiers()&Modifier.PUBLIC)==0) {
				continue;
			}
			
			//has parameters
			if (method.getParameters().length>0) {
				continue;
			}
			
			Optional<String> finalName = ReflectionUtils.convertGetterToCamelcaseField(method.getName());
			
			//not using POJO naming conventions
			if (!finalName.isPresent()) {
				continue;
			}
			
			Validate.notContained(uniquenessNameCheck, finalName.get());
			
			uniquenessNameCheck.add(finalName.get());
			
			fillField(builder, finalName.get(), method.getReturnType(), method.getGenericReturnType().getTypeName(), templateParams, alreadyVisited);
		}
	}

	private void fillField(Builder builder, String name, Class<?> klass, String klassReal, HashMap<String, Type> templateParams, Set<Class<?>> alreadyVisited) {
//		System.out.println("Return type: "+klassReal);
		
		builder.putFields(name, prepareNode(klass, klassReal, templateParams, alreadyVisited));
	}

	private Optional<ValueType> classToValueType(Class<?> klass) {
		if (String.class.isAssignableFrom(klass)) {
			return Optional.of(ValueType.STRING);
		} else if (UUID.class.isAssignableFrom(klass)) {
			return Optional.of(ValueType.STRING);
		} else if (
			BigDecimal.class.isAssignableFrom(klass)
			|| Integer.class.isAssignableFrom(klass)
			|| int.class.isAssignableFrom(klass)
			|| Long.class.isAssignableFrom(klass)
			|| long.class.isAssignableFrom(klass)
			|| Float.class.isAssignableFrom(klass)
			|| float.class.isAssignableFrom(klass)
			|| Double.class.isAssignableFrom(klass)
			|| double.class.isAssignableFrom(klass)
			|| Short.class.isAssignableFrom(klass)
			|| short.class.isAssignableFrom(klass)
			|| Byte.class.isAssignableFrom(klass)
			|| byte.class.isAssignableFrom(klass)
		) {
			return Optional.of(ValueType.NUMBER);
		} else if (Enum.class.isAssignableFrom(klass)) {
			return Optional.of(ValueType.ENUMERABLE);
		} else if (Boolean.class.isAssignableFrom(klass) || boolean.class.isAssignableFrom(klass)) {
			return Optional.of(ValueType.BOOL);
		} else if (LocalDate.class.isAssignableFrom(klass)) {
			return Optional.of(ValueType.DATE);
		} else if (LocalTime.class.isAssignableFrom(klass)) {
			return Optional.of(ValueType.TIME);
		} else if (LocalDateTime.class.isAssignableFrom(klass)) {
			return Optional.of(ValueType.DATETIME);
		} else {
			return Optional.empty();
		}
	}
}