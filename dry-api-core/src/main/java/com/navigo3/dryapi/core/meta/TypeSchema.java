package com.navigo3.dryapi.core.meta;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.immutables.value.Value;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.util.CollectionUtils;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.Validate;

public class TypeSchema {
	
	public enum ContainerType {
		list,
		map,
		optional
	}
	
	public enum ValueType {
		//scalar types
		number,
		string,
		bool,
		enumerable,
		
		//reference types
		ref
	}

	@Value.Immutable
	public interface FieldDefinition {
		String getName();
		
		Optional<ContainerType> getContainerType();
		
		Optional<ValueType> getValueType();
		
		//complex type
		Optional<String> getTypeRef();
	
		//template params
		Optional<List<FieldDefinition>> getTemplateParams();
		
		Optional<List<String>> getEnumValues();
	}

	@Value.Immutable
	public interface TypeDefinition {
		String getName();
		List<FieldDefinition> getFields();
	}
	
	private Map<String, TypeDefinition> definitions = new HashMap<>();
	private String rootDefinition;
	
	public static TypeSchema build(TypeReference<?> type) {
		TypeSchema res = new TypeSchema();
		
		res.parse(type);
		
		return res;
	}

	private TypeSchema() {
	}
	
	public Map<String, TypeDefinition> getDefinitions() {
		return definitions;
	}

	public String getRootDefinition() {
		return rootDefinition;
	}

	private void parse(TypeReference<?> type) {
		final Class<?> klass = (Class<?>)(type.getType() instanceof ParameterizedType ? ((ParameterizedType)type.getType()).getRawType() : type.getType());
	
		rootDefinition = klass.getName();
		
		Set<Class<?>> classesToBeDefined = new HashSet<>();
		classesToBeDefined.add(klass);
		
		parseIteratively(classesToBeDefined);
		
		definitions = Collections.unmodifiableMap(definitions);
	}

	private void parseIteratively(Set<Class<?>> classesToBeDefined) {
		while (!classesToBeDefined.isEmpty()) {
			Class<?> klass = classesToBeDefined.iterator().next();
			classesToBeDefined.remove(klass);

			if (definitions.containsKey(klass.getName())) {
				continue;
			}
			
			ImmutableTypeDefinition.Builder builder = ImmutableTypeDefinition
					.builder()
					.name(klass.getName());
			
			Set<String> uniquenessNameCheck = new HashSet<>();
			
			for (Method method : klass.getMethods()) {
				//is from Object
				if (method.getDeclaringClass().getName().startsWith("java.") || method.getDeclaringClass().getName().startsWith("sun.")) {
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
								
				ImmutableFieldDefinition.Builder fieldBuilder = ImmutableFieldDefinition
					.builder()
					.name(finalName.get());
				
				setupField(method, fieldBuilder, classesToBeDefined);

				builder.addFields(fieldBuilder.build());
			}
			
			TypeDefinition def = builder.build();
			
			definitions.put(def.getName(), def);
		}
	}
	
	private void setupField(Method getter, ImmutableFieldDefinition.Builder fieldBuilder, Set<Class<?>> classesToBeDefined) {
		Class<?> klass = getter.getReturnType();
		
		setupField(klass, getter.getGenericReturnType().getTypeName(),  fieldBuilder, classesToBeDefined);
	}

	private void setupField(Class<?> klass, String returnTypeDesc, ImmutableFieldDefinition.Builder fieldBuilder, Set<Class<?>> classesToBeDefined) {
		if (String.class.isAssignableFrom(klass)) {
			fieldBuilder.valueType(ValueType.string);
		} else if (UUID.class.isAssignableFrom(klass)) {
			fieldBuilder.valueType(ValueType.string);
		} else if (
			klass.isAssignableFrom(BigDecimal.class)
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
			fieldBuilder.valueType(ValueType.number);
		} else if (Enum.class.isAssignableFrom(klass)) {
			fieldBuilder.valueType(ValueType.enumerable);
			
			fieldBuilder.enumValues(Stream.of(klass.getEnumConstants()).map(o->((Enum<?>)o).name()).collect(Collectors.toList()));
		} else if (
			Boolean.class.isAssignableFrom(klass)
			|| boolean.class.isAssignableFrom(klass)
		) {
			fieldBuilder.valueType(ValueType.bool);
		} else if (Optional.class.isAssignableFrom(klass)) {
			fieldBuilder.containerType(ContainerType.optional);
			
			fillTemplateParams(fieldBuilder, klass, returnTypeDesc, classesToBeDefined);
		} else if (Collection.class.isAssignableFrom(klass)) {
			fieldBuilder.containerType(ContainerType.list);
			
			fillTemplateParams(fieldBuilder, klass, returnTypeDesc, classesToBeDefined);
		} else if (Map.class.isAssignableFrom(klass)) {
			fieldBuilder.containerType(ContainerType.map);
			
			fillTemplateParams(fieldBuilder, klass, returnTypeDesc, classesToBeDefined);
		} else {
			if (!definitions.containsKey(klass.getName())) {
				classesToBeDefined.add(klass);
			}
			
			fieldBuilder
				.valueType(ValueType.ref)
				.typeRef(klass.getName());
		}
	}

	private void fillTemplateParams(ImmutableFieldDefinition.Builder fieldBuilder,
			Class<?> klass, String returnTypeDesc, Set<Class<?>> classesToBeDefined) {
		
		List<FieldDefinition> templateParams = CollectionUtils.mapWithIndex(ReflectionUtils.parseTemplateParams(returnTypeDesc), (typeDesc, i)->{
			ImmutableFieldDefinition.Builder templateTypeBuilder = ImmutableFieldDefinition
				.builder()
				.name(klass.getTypeParameters()[i].getName());
			
			String typeKlass = typeDesc.split("<")[0];
			
			ExceptionUtils.withRuntimeException(()->{
				setupField(Class.forName(typeKlass), typeDesc, templateTypeBuilder, classesToBeDefined);
			});
			
			return templateTypeBuilder.build();
		}).collect(Collectors.toList());

		fieldBuilder.templateParams(templateParams);
	}
}

/*

public class DescribeMethodEndpoint extends NavigoApiMethodImplementation<NameParam, MethodDescription> {
	
	private static final Logger logger = LoggerFactory.getLogger(DescribeMethodEndpoint.class);	
	
	private static Set<Class<?>> atomicClasses = new HashSet<>(Arrays.asList(
			String.class,
			Integer.class,
			Boolean.class,
			Long.class,
			Double.class,
			Float.class,
			BigDecimal.class,
			Object.class,
			Class.class,
			Date.class,
			java.sql.Date.class,
			Time.class,
			LocalDate.class,
			LocalDateTime.class,
			LocalTime.class,
			int.class,
			long.class,
			boolean.class,
			double.class,
			float.class
			));
	
	private static Set<String> commonProperties = new HashSet<>(Arrays.asList(
			"class",
			"empty",
			"present",
			"SQLDataType",
			"catalog",
			"schemas",
			"declaringClass",
			"SQLType",
			"arrayDataType",
			"UDT",
			"schema",
			"literal",
			"UDTs",
			"SQLUsable",
			"dataType",
			"array",
			"arrayType"
			));

	@Value.Immutable
	public interface MethodDescription {
		String getName();
		String getDescription();
		TypeDescription getInputType();
		TypeDescription getOutputType();
		RightDescription getSecurityCheck();
		Set<String> getSecurityCheckContextParams();
	}
	
	@Value.Immutable
	public interface RightDescription {
		String getDescription();
		String getKlass();
		List<RightDescription> getChildren();
	}
	
	@Value.Immutable
	public interface TypeDescription {
		String getSimpleType();
		String getType();
		boolean getRecursive();
		Optional<List<TypeDescription>> getGenericTypes();
		Optional<List<PropertyDescription>> getProperties();
	}
	
	@Value.Immutable
	public interface PropertyDescription {
		String getName();
		TypeDescription getType();
	}

	@Override
	public ApiMethod<NameParam, MethodDescription, NavigoContext> getDefinition() {
		return ApiMethodBuilder
			.builder(getClass())
			.inputType(new TypeReference<NameParam>() {})
			.outputType(new TypeReference<MethodDescription>() {})
			.name("metaapi/describe-method")
			.description("Describe API method")
			.authorization(new IsSuperuser())
			.methodResultTypeReference(new TypeReference<MethodResult<MethodDescription>>() {})
			.build();
	}

	@Override
	protected CallContext getCallContext(NameParam input) {
		return null;
	}

	@Override
	protected ValidationResult validateInput(NameParam input) {
		ValidationResult res = new ValidationResult(getContext()::translate);
		
		if (getMethod().getRegistry().lookup(input.getName())==null) {
			res.addResult(ValidationContext.none(), ValidationProblemLevel.error, "Method '{}' does not exist", input.getName());
		}
		
		return res;
	}

	@Override
	protected MethodDescription execute(NameParam input) {		
		ApiMethod<?, ?, ?> apiMethod = getMethod().getRegistry().lookup(input.getName()).get();
		
		return NavigoLambda.withRuntimeException(()->{
			return ImmutableMethodDescription
				.builder()
				.name(apiMethod.getName())
				.description(apiMethod.getDescription())
				.inputType(describeType(apiMethod.getInputType().getType().getTypeName(), new HashSet<Class<?>>(), 0))
				.outputType(describeType(apiMethod.getOutputType().getType().getTypeName(), new HashSet<Class<?>>(), 0))
				.securityCheck(describeSecurityCheck(apiMethod.getSecurityChecker()))
				.securityCheckContextParams(enumerateParams(apiMethod.getSecurityChecker()))
				.build();
		});
	}

	private Set<String> enumerateParams(SecurityChecker<?> securityChecker) {
		HashSet<String> res = new HashSet<>();
		securityChecker.getUsedContextParams().stream().map(ContextParameter::getName).forEach(res::add);
		
		securityChecker.getChildren().forEach(childSecurityChecker->{
			res.addAll(enumerateParams(childSecurityChecker));
		});
		
		return res;
	}

	private TypeDescription describeType(Class<?> klass, Method getter, HashSet<Class<?>> alreadyDescribed, int level) {
		logger.debug("Describing class '{}', level {}", klass, level);
		
		avoidStackOverflow(level);
		
		ImmutableTypeDescription.Builder desc = ImmutableTypeDescription.builder();
		
		fillType(desc, klass);
		
		if (klass.getTypeParameters().length>0 || !alreadyDescribed.contains(klass)) {
			alreadyDescribed.add(klass);
			
			describeProperties(desc, klass, new HashSet<>(alreadyDescribed), level+1);
			
			List<TypeDescription> types = new ArrayList<>();
			
			if (klass.isArray()) {
				types.add(describeType(klass.getComponentType(), null, new HashSet<>(alreadyDescribed), level+1));
			}
			
			TypeDescription forceReturn = NavigoLambda.withRuntimeException(()->{
				if (getter!=null) {
					Type returnType = getter.getGenericReturnType();
					
					if (returnType instanceof ParameterizedType) {
						
						ParameterizedType parametrizedReturnType = (ParameterizedType) returnType;
						
						if (Optional.class.isAssignableFrom(klass) && parametrizedReturnType.getActualTypeArguments().length==1) {
							return describeType(parametrizedReturnType.getActualTypeArguments()[0].getTypeName(), new HashSet<>(alreadyDescribed), level+1);
						} else {
							for (Type t : parametrizedReturnType.getActualTypeArguments()) {
								types.add(describeType(t.getTypeName(), new HashSet<>(alreadyDescribed), level+1));
							}
						}
					}
				}
				
				return null;
			});
			
			if (forceReturn!=null) {
				return forceReturn;
			}
			
			if (!types.isEmpty()) {
				desc.genericTypes(types);
			}
		} else {
			desc.recursive(true);
		}
		
		return desc.build();
	}

	private TypeDescription describeType(String klassDef, HashSet<Class<?>> alreadyDescribed, int level) throws ClassNotFoundException {
		logger.debug("Describing string '{}', level {}", klassDef, level);
		
		avoidStackOverflow(level);
		
		GenericParseResult parseResult = NavigoReflectionUtils.parseGenericType(klassDef);
		
		Class<?> klass = getClass().getClassLoader().loadClass(parseResult.getType());
		
		Validate.notNull(klass);
		
		if (Optional.class.isAssignableFrom(klass) && parseResult.getTemplateParams().size()==1) {
			return describeType(parseResult.getTemplateParams().get(0), new HashSet<>(alreadyDescribed), level+1);
		}
		
		ImmutableTypeDescription.Builder desc = ImmutableTypeDescription.builder();
		
		fillType(desc, klass);
		
		if (klass.getTypeParameters().length>0 || !alreadyDescribed.contains(klass)) {
			alreadyDescribed.add(klass);
		
			describeProperties(desc, klass, new HashSet<>(alreadyDescribed), level+1);
			
			List<TypeDescription> types = new ArrayList<>();
			
			for (String sub : parseResult.getTemplateParams()) {
				types.add(describeType(sub, new HashSet<>(alreadyDescribed), level+1));
			}	
			
			if (!types.isEmpty()) {
				desc.genericTypes(types);
			}
		} else {
			desc.recursive(true);
		}

		return desc.build();
	}
	
	private void fillType(Builder desc, Class<?> klass) {
		desc.type(klass.getCanonicalName());
		desc.recursive(false);
		
		if (isBasicType(klass)) {
			desc.simpleType(klass.getSimpleName());
		} else if (Collection.class.isAssignableFrom(klass) || klass.isArray()) {
			desc.simpleType("[]");
		} else {
			desc.simpleType("{}");
		}
	}

	private void describeProperties(Builder desc, Class<?> klass, HashSet<Class<?>> alreadyDescribed, int level) {
		logger.debug("Describing properties of '{}', level {}", klass, level);
		
		avoidStackOverflow(level);
		
		List<PropertyDescription> props = new ArrayList<>();
		
		NavigoLambda.withRuntimeException(()->{
			if (!isBasicType(klass)) {
				
				BeanInfo bi = Introspector.getBeanInfo(klass);

				for (PropertyDescriptor prop : bi.getPropertyDescriptors()) {
					if (!isCommonProperty(prop.getName())) {
						Method propGetter = prop.getReadMethod();

						logger.debug("Describing property {}, level {}", prop.getName(), level);
						
						props.add(ImmutablePropertyDescription
								.builder()
								.name(prop.getName())
								.type(describeType(propGetter.getReturnType(), propGetter, new HashSet<>(alreadyDescribed), level+1))
								.build());
					}
				}
			}
		});
		
		if (!props.isEmpty()) {
			desc.properties(props);
		}
	}

	private void avoidStackOverflow(int level) {
		if (level>100) {
			throw new RuntimeException("Too deep structure! Loop?");
		}
	}

	private boolean isCommonProperty(String name) {
		return commonProperties.contains(name);
	}

	private boolean isBasicType(Class<?> klass) {
		return atomicClasses.contains(klass);
	}

	private RightDescription describeSecurityCheck(SecurityChecker<?> securityChecker) {
		ImmutableRightDescription.Builder desc = ImmutableRightDescription.builder();
		desc.description(securityChecker.getDescription());
		desc.klass(securityChecker.getClass().getCanonicalName());
	
		securityChecker.getChildren().forEach(c->{
			desc.addChildren(describeSecurityCheck(c));
		});
		
		return desc.build();
	}
}

 */