package com.navigo3.dryapi.core.meta;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import org.immutables.value.Value;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.path.TypePathItem;
import com.navigo3.dryapi.core.path.TypeSelectorType;
import com.navigo3.dryapi.core.util.CollectionUtils;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.JacksonUtils;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

/***
 * This class converts Java type into JSON structure metadata. When Java instance is converted into JSON by 
 * Jackson library, it should have structure described by this metadata.
 */
public class TypeSchema {
	
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
		
		//reference types
		REF
	}
	
	@Value.Immutable
	public interface NamedTypeMeta {
		String getName();
		
		Optional<ContainerType> getContainerType();

		List<NamedTypeMeta> getTemplateParams();
		
		Optional<ValueType> getValueType();
		
		//complex type
		Optional<String> getTypeRef();
	
		List<String> getEnumValues();
	}

	@Value.Immutable
	public interface TypeMeta {
		String getName();
		
		Optional<ContainerType> getContainerType();

		List<NamedTypeMeta> getTemplateParams();
		
		List<NamedTypeMeta> getFields();
		
		@Value.Default default boolean getIsDirectRootContainer() {
			return false;
		};
	}

	private static final String ROOT_CONTAINER_ID = "DirectRootContainter";
	
	private Map<String, TypeMeta> definitions = new HashMap<>();
	private String rootDefinition;
	
	public static TypeSchema build(TypeReference<?> type) {
		TypeSchema res = new TypeSchema();
		
		res.parse(type);
		
		return res;
	}

	private TypeSchema() {
	}
	
	public Map<String, TypeMeta> getDefinitions() {
		return definitions;
	}

	public String getRootDefinition() {
		return rootDefinition;
	}

	private void parse(TypeReference<?> type) {
		final Class<?> klass;
		final Type[] templateParams;
		
		HashMap<String, String> templateParamTypes = new HashMap<>();
		
		if (type.getType() instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType)type.getType();
			
			klass = (Class<?>)paramType.getRawType();
			templateParams = paramType.getActualTypeArguments();
			
			for (int i=0;i<templateParams.length;++i) {
				templateParamTypes.put(klass.getTypeParameters()[i].getName(), templateParams[i].getTypeName());
			}
		} else {
			klass = (Class<?>)type.getType();
			templateParams = new Type[]{};
		}

		rootDefinition = klass.getName();
		
		Set<Class<?>> classesToBeDefined = new HashSet<>();
		classesToBeDefined.add(klass);
		
		parseIteratively(classesToBeDefined, templateParams, templateParamTypes);
		
		definitions = Collections.unmodifiableMap(definitions);
	}

	private void parseIteratively(Set<Class<?>> classesToBeDefined, Type[] templateParams, HashMap<String, String> templateParamTypes) {
		boolean first = true;
		
		while (!classesToBeDefined.isEmpty()) {
			Class<?> klass = classesToBeDefined.iterator().next();
			classesToBeDefined.remove(klass);

			if (definitions.containsKey(klass.getName())) {
				continue;
			}
			
			ImmutableTypeMeta.Builder builder = ImmutableTypeMeta.builder();

			if (Collection.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.LIST);
			} else if (Map.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.MAP);
			} else if (Optional.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.OPTIONAL);
			} else {
				builder.name(klass.getName());
			}
			
			if (first) {
				List<NamedTypeMeta> typeFields = new ArrayList<>();
				
				builder
					.name(ROOT_CONTAINER_ID)
					.isDirectRootContainer(true);
				
				rootDefinition = ROOT_CONTAINER_ID;
		
				for (Type type : klass.getTypeParameters()) {
					ImmutableNamedTypeMeta.Builder fieldBuilder = ImmutableNamedTypeMeta.builder();
					fieldBuilder.name(type.getTypeName());
					
					String typeKlass = templateParams[typeFields.size()].getTypeName().split("<")[0];
					
					ExceptionUtils.withRuntimeException(()->{
						setupField(Class.forName(typeKlass), templateParams[typeFields.size()].getTypeName(), fieldBuilder, classesToBeDefined, templateParamTypes);
					});
					
					typeFields.add(fieldBuilder.build());
				}
				
				builder.templateParams(typeFields);
			}
			
			first = false;
			
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
								
				ImmutableNamedTypeMeta.Builder fieldBuilder = ImmutableNamedTypeMeta
					.builder()
					.name(finalName.get());
				
				setupField(method, fieldBuilder, classesToBeDefined, templateParamTypes);

				builder.addFields(fieldBuilder.build());
			}
			
			TypeMeta def = builder.build();
			
			definitions.put(def.getName(), def);
		}
	}
	
	private void setupField(Method getter, ImmutableNamedTypeMeta.Builder fieldBuilder, Set<Class<?>> classesToBeDefined, Map<String, String> templateParamTypes) {
		Class<?> klass = getter.getReturnType();
	
		setupField(klass, getter.getGenericReturnType().getTypeName(),  fieldBuilder, classesToBeDefined, templateParamTypes);
	}

	private void setupField(Class<?> klass, String returnTypeDesc, ImmutableNamedTypeMeta.Builder fieldBuilder, Set<Class<?>> classesToBeDefined, Map<String, String> templateParamTypes) {
		Optional<ValueType> valueType = classToValueType(klass);
		
		if (valueType.isPresent()) {
			fieldBuilder.valueType(valueType.get());
			
			if (valueType.get()==ValueType.ENUMERABLE) {
				fieldBuilder.enumValues(Stream.of(klass.getEnumConstants()).map(o->((Enum<?>)o).name()).sorted().collect(Collectors.toList()));
			}
		} else if (Optional.class.isAssignableFrom(klass)) {
			fieldBuilder.containerType(ContainerType.OPTIONAL);
			
			fillTemplateParams(fieldBuilder, klass, returnTypeDesc, classesToBeDefined, false);
		} else if (Collection.class.isAssignableFrom(klass)) {
			fieldBuilder.containerType(ContainerType.LIST);
			
			fillTemplateParams(fieldBuilder, klass, returnTypeDesc, classesToBeDefined, false);
		} else if (klass.isArray()) {
			fieldBuilder.containerType(ContainerType.LIST);
			
			Optional<ValueType> paramValueType = classToValueType(klass.getComponentType());

			fieldBuilder.addTemplateParams(ImmutableNamedTypeMeta
				.builder()
				.name("T")
				.valueType(paramValueType)
				.typeRef(paramValueType.isPresent() ? Optional.empty() : Optional.of(klass.getComponentType().getName()))
				.build()
			);
			
			if (!paramValueType.isPresent()) {
				ExceptionUtils.withRuntimeException(()->{
					classesToBeDefined.add(Class.forName(klass.getComponentType().getName()));
				});
			}
		} else if (Map.class.isAssignableFrom(klass)) {
			fieldBuilder.containerType(ContainerType.MAP);
			
			fillTemplateParams(fieldBuilder, klass, returnTypeDesc, classesToBeDefined, true);
		} else {
			String returnTypeDescReal = returnTypeDesc.split("<")[0];
			
//			System.out.println(klass.getName()+" vs "+returnTypeDescReal);

			if (klass.getName().equals(returnTypeDescReal)) {
				if (!definitions.containsKey(klass.getName())) {
					classesToBeDefined.add(klass);
				}
				
				fieldBuilder
					.valueType(ValueType.REF)
					.typeRef(klass.getName());
			} else {
				Validate.keyContained(templateParamTypes, returnTypeDescReal, StringUtils.subst("Missing field {} but there are [{}]", returnTypeDescReal, templateParamTypes.keySet().stream().collect(Collectors.joining(", "))));
				
				String klassNameRealFull = templateParamTypes.get(returnTypeDescReal);
				String klassNameReal = klassNameRealFull.split("<")[0];
				
				Class<?> klassSub = ExceptionUtils.withRuntimeException(()->Class.forName(klassNameReal));
		
				if (!definitions.containsKey(klassNameReal)) {
					classesToBeDefined.add(klassSub);
				}
				
				fieldBuilder
					.valueType(ValueType.REF)
					.typeRef(klassNameReal);
				
				fillTemplateParams(fieldBuilder, klassSub, klassNameRealFull, classesToBeDefined, false);
			}
		}
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

	private void fillTemplateParams(ImmutableNamedTypeMeta.Builder fieldBuilder,
			Class<?> klass, String returnTypeDesc, Set<Class<?>> classesToBeDefined, boolean isMap) {
		
		List<NamedTypeMeta> templateParams = CollectionUtils.mapWithIndex(ReflectionUtils.parseTemplateParams(returnTypeDesc), (typeDesc, i)->{
			ImmutableNamedTypeMeta.Builder templateTypeBuilder = ImmutableNamedTypeMeta
				.builder()
				.name(klass.getTypeParameters()[i].getName());
			
			String typeKlass = typeDesc.split("<")[0];
			
			if (isMap && i==0 && !typeKlass.equals("java.lang.String")) {
				System.err.println("\tExpected string as key, got "+typeKlass);
			}
			
			ExceptionUtils.withRuntimeException(()->{
				setupField(Class.forName(typeKlass), typeDesc, templateTypeBuilder, classesToBeDefined, new HashMap<>());
			});
			
			return templateTypeBuilder.build();
		}).collect(Collectors.toList());

		fieldBuilder.templateParams(templateParams);
	}

	public void throwIfPathNotExists(TypePath path) {
		TypeMeta actType = definitions.get(rootDefinition);
		NamedTypeMeta actField = null;

		for (int i=0;i<path.getItems().size();++i) {
			TypePathItem item = path.getItems().get(i);

			try {
				Validate.oneNotNull(actType, actField);

				if (item.getType()==TypeSelectorType.KEEP_RECURSIVELY) {
					Validate.equals(i+1, path.getItems().size());
					return;
				}
				
				if (actType!=null && actType.getIsDirectRootContainer()) {
					Validate.isPresent(actType.getContainerType());
					
					if (actType.getContainerType().get()==ContainerType.OPTIONAL) {
						Validate.size(actType.getTemplateParams(), 1);
						
						actField = actType.getTemplateParams().get(0);
						actType = null;
					}
				}
				
				if (actType!=null) {
					if (actType.getIsDirectRootContainer()) {
						Validate.isPresent(actType.getContainerType());
						
						if (actType.getContainerType().get()==ContainerType.LIST) {
							Validate.equals(item.getType(), TypeSelectorType.INDEX);
							Validate.size(actType.getTemplateParams(), 1);
							
							actField = actType.getTemplateParams().get(0);
							actType = null;
						} else if (actType.getContainerType().get()==ContainerType.MAP) {
							Validate.equals(item.getType(), TypeSelectorType.KEY);
							Validate.size(actType.getTemplateParams(), 2);
							
							actField = actType.getTemplateParams().get(1);
							actType = null;
						} else {
							throw new RuntimeException(StringUtils.subst("Unexpected container type {}", actType.getContainerType().get()));
						}
					} else {
						Validate.equals(item.getType(), TypeSelectorType.FIELD);
						
						Optional<NamedTypeMeta> field = actType.getFields().stream().filter(f->f.getName().equals(item.getFieldName().get())).findFirst();
						
						Validate.isPresent(field);
						
						actField = field.get();
						actType = null;
					}			
				} else {
					while (actField.getContainerType().isPresent() && actField.getContainerType().get()==ContainerType.OPTIONAL) {
						Validate.size(actField.getTemplateParams(), 1);
						
						actField = actField.getTemplateParams().get(0);
					}
					
					Validate.isPresent(actField.getContainerType());
					
					if (actField.getContainerType().isPresent() && actField.getContainerType().get()==ContainerType.LIST) {
						Validate.equals(item.getType(), TypeSelectorType.INDEX);
						Validate.size(actField.getTemplateParams(), 1);
						
						actField = actField.getTemplateParams().get(0);
					} else if (actField.getContainerType().isPresent() && actField.getContainerType().get()==ContainerType.MAP) {
						Validate.equals(item.getType(), TypeSelectorType.KEY);
						Validate.size(actField.getTemplateParams(), 2);
						
						actField = actField.getTemplateParams().get(1);
					} else {
						throw new RuntimeException(StringUtils.subst("Unexpected container type {}", actField.getContainerType().get()));
					}
				}
				
				while (actField!=null && actField.getContainerType().isPresent() && actField.getContainerType().get()==ContainerType.OPTIONAL) {
					Validate.size(actField.getTemplateParams(), 1);
					
					actField = actField.getTemplateParams().get(0);
				}
				
				if (actField.getTypeRef().isPresent()) {
					actType = definitions.get(actField.getTypeRef().get());
					actField = null;
				}	
			} catch (Throwable t) {
				throw new RuntimeException(StringUtils.subst("Problem with path on item {} of schema {}", path.getDebug(i), JacksonUtils.prettyGet(this)), t);
			}
		}
		
		Validate.isTrue(actField!=null && actType==null, "It is expected last item will select field");
		Validate.isTrue(actField.getValueType().isPresent(), "It seems path is too short and ends in collection, not field");
	}
	
	public void debugPrint(PrintStream out) {
		Set<String> alreadyPrintedDefs = new HashSet<>();
		
		printDefinition(out, getDefinitions().get(getRootDefinition()), 0, alreadyPrintedDefs);
	}

	private void printDefinition(PrintStream out, TypeMeta def, int depth, Set<String> alreadyPrintedDefs) {
		if (alreadyPrintedDefs.contains(def.getName())) {
			out.println(StringUtils.repeat(depth, "    ")+" RECURSIVE "+getTypeLabel(def));
			
			return;
		}
		
		alreadyPrintedDefs.add(def.getName());
		
		out.println(StringUtils.repeat(depth, "    ")+getTypeLabel(def));
		
		def.getTemplateParams().forEach(par->{
			printField(out, depth+1, par, alreadyPrintedDefs);
		});
		
		def.getFields().stream().sorted(Comparator.comparing(NamedTypeMeta::getName)).forEach(field->{
			printField(out, depth+1, field, alreadyPrintedDefs);
		});
		
		alreadyPrintedDefs.remove(def.getName());
	}
	
	private void printField(PrintStream out, int depth, NamedTypeMeta namedType, Set<String> alreadyPrintedDefs) {
		out.println(StringUtils.repeat(depth, "    ")+getFieldLabel(namedType));
		
		if (namedType.getTypeRef().isPresent()) {
			String ref = namedType.getTypeRef().get();
			printDefinition(out, getDefinitions().get(ref), depth+1, alreadyPrintedDefs);
		} else if (namedType.getValueType().isPresent()) {
			//
		} else if (namedType.getContainerType().isPresent()) {
			namedType.getTemplateParams().forEach(templateParam->printField(out, depth+1, templateParam, alreadyPrintedDefs));
		} else {
			throw new RuntimeException("Unknown State");
		}
	}

	private String getFieldLabel(NamedTypeMeta field) {
		StringBuilder res = new StringBuilder();
		
		res.append(field.getName());
		
		appendContainerType(res, field.getContainerType(), field.getTemplateParams());
		
		field.getValueType().ifPresent(valueType->{
			res.append(" : "+valueType.name());
			if (valueType==ValueType.ENUMERABLE) {
				res.append(" OF ["+field.getEnumValues().stream().sorted().collect(Collectors.joining(","))+"]");
			}
		});
		
		return res.toString();
	}
	
	private String getTypeLabel(TypeMeta type) {
		StringBuilder res = new StringBuilder();
		
		res.append(type.getName());
		
		appendContainerType(res, type.getContainerType(), type.getTemplateParams());

		return res.toString();
	}

	private void appendContainerType(StringBuilder res, Optional<ContainerType> containerType, List<NamedTypeMeta> teplateParams) {
		String types = teplateParams.stream().map(p->p.getName()).collect(Collectors.joining(", "));
		
		containerType.ifPresent(containterType->{
			if (containterType==ContainerType.OPTIONAL) {
				res.append("(?"+types+"?)");
			} else if (containterType==ContainerType.LIST) {
				res.append("["+types+"]");
			} else if (containterType==ContainerType.MAP) {
				res.append("{"+types+"}");
			} else {
				throw new RuntimeException("Unknown type "+containterType);
			}
		});
		
		if (!containerType.isPresent()) {
			res.append("<"+types+">");
		}
	}
}