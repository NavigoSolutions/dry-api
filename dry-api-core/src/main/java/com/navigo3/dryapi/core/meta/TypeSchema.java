package com.navigo3.dryapi.core.meta;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.navigo3.dryapi.core.doc.ApiField;
import com.navigo3.dryapi.core.meta.NodeMetadata.ContainerType;
import com.navigo3.dryapi.core.meta.NodeMetadata.ValueType;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.path.TypePathItem;
import com.navigo3.dryapi.core.path.TypeSelectorType;
import com.navigo3.dryapi.core.util.CollectionUtils;
import com.navigo3.dryapi.core.util.DryApiConstants;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

/***
 * This class converts Java type into JSON structure metadata. When Java
 * instance is converted into JSON by Jackson library, it should have structure
 * described by this metadata.
 */
public class TypeSchema {
	private static final Logger logger = LoggerFactory.getLogger(TypeSchema.class);

	private static final Map<String, String> valueTypeToClass = new HashMap<>();

	static {
		valueTypeToClass.put("byte", Byte.class.getName());
		valueTypeToClass.put("short", Short.class.getName());
		valueTypeToClass.put("int", Integer.class.getName());
		valueTypeToClass.put("long", Long.class.getName());
		valueTypeToClass.put("float", Float.class.getName());
		valueTypeToClass.put("double", Double.class.getName());
		valueTypeToClass.put("char", Character.class.getName());
		valueTypeToClass.put("boolean", Boolean.class.getName());
	}

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
			if (actNode != root) {
				fullPath.append(" -> ");
			}

			fullPath.append(item.getDebug());

			if (item.getType() == TypeSelectorType.FIELD) {
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
			} else if (item.getType() == TypeSelectorType.INDEX) {
				Validate.isPresent(actNode.getContainerType(), "Expected container at path {}", fullPath);
				Validate.equals(
					actNode.getContainerType().get(),
					ContainerType.LIST,
					"Expected array at path {}",
					fullPath
				);

				actNode = actNode.getItemType().get();
			} else if (item.getType() == TypeSelectorType.KEY) {
				Validate.isPresent(actNode.getContainerType(), "Expected container at path {}", fullPath);
				Validate.equals(
					actNode.getContainerType().get(),
					ContainerType.MAP,
					"Expected map at path {}",
					fullPath
				);

				actNode = actNode.getItemType().get();
			} else if (item.getType() == TypeSelectorType.KEEP_RECURSIVELY) {
				return;
			} else {
				throw new RuntimeException("Unexpected type " + item.getType().name());
			}

			if (actNode.getContainerType().isPresent() && actNode.getContainerType().get() == ContainerType.OPTIONAL) {
				actNode = actNode.getItemType().get();
			}
		}

		if (actNode.getContainerType().isPresent() || actNode.getValueType().get() == ValueType.OBJECT) {
			throw new RuntimeException("This path does not end on value node!");
		}
	}

	public void debugPrint() {
		ExceptionUtils.withRuntimeException(() -> {
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

	private void debugPrint(PrintStream out, NodeMetadata node, Optional<String> name, int indentLevel,
		String indentStr) {
		out.print(StringUtils.repeat(indentLevel, indentStr));

		node.getContainerType().ifPresent(type -> {
			if (type == ContainerType.LIST) {
				out.print("[]");
			} else if (type == ContainerType.MAP) {
				out.print("{" + node.getKeyType().get().name() + "}");
			} else if (type == ContainerType.OPTIONAL) {
				out.print("(?)");
			} else {
				throw new RuntimeException("Unsupported container type " + type.name());
			}
		});

		node.getValueType().ifPresent(t -> {
			out.print(t.name());

			if (t == ValueType.ENUMERABLE) {
				out.print("[" + node.getEnumItems().stream().collect(Collectors.joining(", ")) + "]");
			}

			if (t == ValueType.OBJECT) {
				out.print(" " + node.getJavaType());
			}
		});

		name.ifPresent(s -> out.print(" " + s));

		out.println();

		node.getContainerType().ifPresent(type -> {
			debugPrint(out, node.getItemType().get(), Optional.empty(), indentLevel + 1, indentStr);
		});

		node.getFields().entrySet().stream().sorted(Comparator.comparing(e -> e.getKey())).forEach(e -> {
			debugPrint(out, e.getValue(), Optional.of(e.getKey()), indentLevel + 1, indentStr);
		});
	}

	private void parse(TypeReference<?> type) {
		root = prepareNode(type.getType().getTypeName(), new HashSet<>(), Optional.empty());
	}

	private NodeMetadata prepareNode(String klassReal, Set<Class<?>> alreadyVisited, Optional<Method> optMethod) {
		if (klassReal.equals(JsonNode.class.getName())) {
			return ImmutableNodeMetadata.builder().valueType(ValueType.JSON).javaType(klassReal).build();
		}

		ImmutableNodeMetadata.Builder builder = ImmutableNodeMetadata.builder();
		builder.javaType(klassReal);
		var apiFieldAnnotation = optMethod.flatMap(method -> Optional.ofNullable(method.getAnnotation(ApiField.class)));

		apiFieldAnnotation.ifPresent(a -> {
			Validate.isFalse(!a.deprecated() && !a.deprecatedComment().isEmpty());

			builder.securityMessage(Optional.of(a.extraSecurity()).filter(m -> !m.isBlank()))
				.description(Optional.of(a.description()).filter(m -> !m.isBlank()))
				.deprecated(a.deprecated())
				.deprecatedComment(Optional.of(a.deprecatedComment()).filter(v -> !v.isEmpty()));
		});

		if (klassReal.endsWith("[]")) {
			builder.containerType(ContainerType.LIST);

			builder.itemType(prepareNode(klassReal.split("\\[")[0], alreadyVisited, Optional.empty()));

			apiFieldAnnotation.ifPresent(a -> {
				applyIfSet(a.minLength(), builder::minLength);
				applyIfSet(a.maxLength(), builder::maxLength);
			});

			return builder.build();
		}

		Class<?> klass = ExceptionUtils.withRuntimeException(() -> Class.forName(klassReal.split("<")[0]));

		if (alreadyVisited.contains(klass)) {
			return ImmutableNodeMetadata.builder().valueType(ValueType.RECURSIVE).javaType(klassReal).build();
		}

		Optional<ValueType> optValueType = classToValueType(klass);

		boolean hasDefaultValue = apiFieldAnnotation.isPresent()
			&& (!apiFieldAnnotation.get().defaultValue().isEmpty()
				|| optValueType.map(ValueType.STRING::equals).orElse(false));

		apiFieldAnnotation.ifPresent(a -> {
			if (hasDefaultValue) {
				builder.defaultValue(a.defaultValue());
			}
		});

		optValueType.ifPresent(valueType -> {

			switch (valueType) {
			case ENUMERABLE:
				var enumItems = Stream.of(klass.getEnumConstants())
					.map(o -> ((Enum<?>) o).name())
					.sorted()
					.collect(Collectors.toList());

				apiFieldAnnotation.ifPresent(a -> {

					var allowedValues = Set.of(a.allowedValues());
					allowedValues.forEach(key -> Validate.contained(enumItems, key));

					if (!allowedValues.isEmpty()) {
						enumItems.removeIf(k -> !allowedValues.contains(k));
					}

					if (hasDefaultValue) {
						Validate.contained(enumItems, a.defaultValue());
					}
				});

				builder.enumItems(enumItems);

				break;
			case DATE:
				builder.format(DryApiConstants.DATE_FORMAT);
				break;
			case DATETIME:
				builder.format(DryApiConstants.DATETIME_FORMAT);
				break;
			case TIME:
				builder.format(DryApiConstants.TIME_FORMAT);
				break;
			case NUMBER:
				apiFieldAnnotation.ifPresent(a -> {
					applyIfNotBlank(a.min(), v -> builder.minValue(new BigDecimal(v)));
					applyIfNotBlank(a.max(), v -> builder.maxValue(new BigDecimal(v)));
				});
				break;
			case STRING:
				apiFieldAnnotation.ifPresent(a -> {
					applyIfSet(a.minLength(), builder::minLength);
					applyIfSet(a.maxLength(), builder::maxLength);
					applyIfNotBlank(a.pattern(), builder::pattern);
					applyIfNotBlank(a.format(), builder::format);
				});
				break;

			default:

				break;

			}

			builder.valueType(valueType);
		});

		if (!optValueType.isPresent()) {
			if (Optional.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.OPTIONAL);

				List<String> params = ReflectionUtils.parseTemplateParams(klassReal);
				Validate.size(params, 1);

				builder.itemType(prepareNode(params.get(0), alreadyVisited, Optional.empty()));
			} else if (Collection.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.LIST);

				List<String> params = ReflectionUtils.parseTemplateParams(klassReal);
				Validate.size(params, 1);

				builder.itemType(prepareNode(params.get(0), alreadyVisited, Optional.empty()));
			} else if (Map.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.MAP);

				List<String> params = ReflectionUtils.parseTemplateParams(klassReal);
				Validate.size(params, 2);

				String paramClassNameK = params.get(0).split("<")[0];

				Optional<ValueType> keyType = ExceptionUtils.withRuntimeException(
					() -> classToValueType(Class.forName(paramClassNameK))
				);

				if (!keyType.isPresent()) {
					logger.warn(
						"Implausible map key type '{}'. Are you sure it has correct toString() method?",
						paramClassNameK
					);
				}

				builder.keyType(keyType.orElse(ValueType.OBJECT));

				builder.itemType(prepareNode(params.get(1), alreadyVisited, Optional.empty()));
			} else {
				builder.valueType(ValueType.OBJECT);

				alreadyVisited.add(klass);

				Map<String, Map<String, String>> templateParams = new HashMap<>();

				CollectionUtils.eachWithIndex(ReflectionUtils.parseTemplateParams(klassReal), (param, i) -> {
					templateParams.computeIfAbsent(klassReal.split("<")[0], k -> new HashMap<String, String>())
						.put(klass.getTypeParameters()[i].getName(), param);
				});

				if (klass.getGenericSuperclass() != null) {
					addDeclTypesRec(
						templateParams,
						klass.getGenericSuperclass().getTypeName(),
						templateParams.getOrDefault(klassReal, Map.of())
					);
				}

				for (Type intType : klass.getGenericInterfaces()) {
					addDeclTypesRec(
						templateParams,
						intType.getTypeName(),
						templateParams.getOrDefault(klassReal, Map.of())
					);
				}

				fillFields(builder, klass, templateParams, alreadyVisited);

				alreadyVisited.remove(klass);
			}
		}

		return builder.build();
	}

	private void addDeclTypesRec(Map<String, Map<String, String>> res, String klassReal,
		Map<String, String> derivedClassMapping) {
		Class<?> klass = ExceptionUtils.withRuntimeException(() -> Class.forName(klassReal.split("<")[0]));

		CollectionUtils.eachWithIndex(ReflectionUtils.parseTemplateParams(klassReal), (param, i) -> {
			String actualVal;

// we expect that java class name contains a dot
			if (param.contains(".")) {
				actualVal = param;
			} else {
				Validate.keyContained(
					derivedClassMapping,
					klass.getTypeParameters()[i].getName(),
					StringUtils.subst(
						"Unexpected state - template parameter not materialized '{}' ?!?",
						klass.getTypeParameters()[i].getName()
					)
				);

				actualVal = derivedClassMapping.get(klass.getTypeParameters()[i].getName());
			}

			res.computeIfAbsent(klass.getName(), k -> new HashMap<String, String>())
				.put(klass.getTypeParameters()[i].getName(), actualVal);
		});

		if (klass.getGenericSuperclass() != null) {
			addDeclTypesRec(
				res,
				klass.getGenericSuperclass().getTypeName(),
				res.getOrDefault(klass.getName(), Map.of())
			);
		}

		for (Type intType : klass.getGenericInterfaces()) {
			addDeclTypesRec(res, intType.getTypeName(), res.getOrDefault(klass.getName(), Map.of()));
		}
	}

	private void fillFields(ImmutableNodeMetadata.Builder builder, Class<?> klass,
		Map<String, Map<String, String>> templateParams, Set<Class<?>> alreadyVisited) {
		Set<String> uniquenessNameCheck = new HashSet<>();

		for (Method method : klass.getMethods()) {

// core packages
			if (method.getDeclaringClass().getName().startsWith("sun.")
				|| method.getDeclaringClass().getName().startsWith("java.")) {
				continue;
			}

// is not public
			if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
				continue;
			}

// is static
			if ((method.getModifiers() & Modifier.STATIC) != 0) {
				continue;
			}

// has parameters
			if (method.getParameters().length > 0) {
				continue;
			}

// ignored field
			if (method.getAnnotation(JsonIgnore.class) != null) {
				continue;
			}

			Optional<String> finalName = ReflectionUtils.convertGetterToCamelcaseField(method.getName());

// not using POJO naming conventions
			if (!finalName.isPresent()) {
				continue;
			}

			Validate.notContained(uniquenessNameCheck, finalName.get());

			uniquenessNameCheck.add(finalName.get());

			fillField(builder, finalName.get(), method, templateParams, alreadyVisited);
		}
	}

	private void fillField(ImmutableNodeMetadata.Builder builder, String name, Method method,
		Map<String, Map<String, String>> templateParams, Set<Class<?>> alreadyVisited) {

		String klassReal = substituteTemplateParams(
			method.getGenericReturnType().getTypeName(),
			templateParams,
			method.getDeclaringClass().getName()
		);

		Class<?> klass = loadClassOrDefault(klassReal.split("<")[0], method.getReturnType());

		HashMap<String, Type> subTemplateParams = new HashMap<>();

		List<String> parsedParams = ReflectionUtils.parseTemplateParams(klassReal);

		CollectionUtils.eachWithIndex(parsedParams, (paramType, i) -> {
			String paramName = klass.getTypeParameters()[i].getName();

			ExceptionUtils.withRuntimeException(() -> {
				subTemplateParams.put(paramName, Class.forName(paramType.split("<")[0]));
			});
		});

		klassReal = valueTypeToClass.getOrDefault(klassReal, klassReal);

		NodeMetadata res = prepareNode(klassReal, alreadyVisited, Optional.of(method));

		builder.putFields(name, res);

	}

	private Class<?> loadClassOrDefault(String className, Class<?> defaultVal) {
		try {
			return Class.forName(className.split("<")[0]);
		} catch (Throwable t) {
			return defaultVal;
		}
	}

	private String substituteTemplateParams(String typeName, Map<String, Map<String, String>> templateParams,
		String declaringName) {
		String res = typeName;

		if (!templateParams.containsKey(declaringName)) {
			return res;
		}

		Validate.keyContained(templateParams, declaringName);

		for (Entry<String, String> entry : templateParams.get(declaringName).entrySet()) {
			String regex = StringUtils.subst("(?<![a-zA-Z_]){}(?![a-zA-Z0-9_])", Pattern.quote(entry.getKey()));

			res = res.replaceAll(regex, Matcher.quoteReplacement(entry.getValue()));
		}

		return res;
	}

	private Optional<ValueType> classToValueType(Class<?> klass) {
		if (String.class.isAssignableFrom(klass)) {
			return Optional.of(ValueType.STRING);
		} else if (UUID.class.isAssignableFrom(klass)) {
			return Optional.of(ValueType.STRING);
		} else if (BigDecimal.class.isAssignableFrom(klass)
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
			|| byte.class.isAssignableFrom(klass)) {
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

	private static void applyIfNotBlank(String value, Consumer<String> setter) {
		if (!value.isBlank()) {
			setter.accept(value);
		}
	}

	private static void applyIfSet(int value, Consumer<Integer> setter) {
		if (value != -1) {
			setter.accept(value);
		}
	}

}