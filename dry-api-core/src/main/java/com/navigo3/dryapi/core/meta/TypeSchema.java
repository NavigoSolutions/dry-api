package com.navigo3.dryapi.core.meta;

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
import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.core.util.ReflectionUtils;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;

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
		
		Optional<ContainerType> getContainerType();
		
		Optional<List<FieldDefinition>> getTemplateParams();
		
		List<FieldDefinition> getFields();
		
		@Value.Default default boolean getIsDirectRootContainer() {
			return false;
		};
	}

	private static final String ROOT_CONTAINER_ID = "DirectRootContainter";
	
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
		final Class<?> klass;
		final Type[] templateParams;
		
		if (type.getType() instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType)type.getType();
			
			klass = (Class<?>)paramType.getRawType();
			templateParams = paramType.getActualTypeArguments();
		} else {
			klass = (Class<?>)type.getType();
			templateParams = new Type[]{};
		}

		rootDefinition = klass.getName();
		
		Set<Class<?>> classesToBeDefined = new HashSet<>();
		classesToBeDefined.add(klass);
		
		parseIteratively(classesToBeDefined, templateParams);
		
		definitions = Collections.unmodifiableMap(definitions);
	}

	private void parseIteratively(Set<Class<?>> classesToBeDefined, Type[] templateParams) {
		boolean first = true;
		
		while (!classesToBeDefined.isEmpty()) {
			Class<?> klass = classesToBeDefined.iterator().next();
			classesToBeDefined.remove(klass);

			if (definitions.containsKey(klass.getName())) {
				continue;
			}
			
			ImmutableTypeDefinition.Builder builder = ImmutableTypeDefinition.builder();
			
			boolean isKnownTemplate = true;
			
			if (Collection.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.LIST);
			} else if (Map.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.MAP);
			} else if (Optional.class.isAssignableFrom(klass)) {
				builder.containerType(ContainerType.OPTIONAL);
			} else {
				isKnownTemplate = false;
				builder.name(klass.getName());
			}
			
			if (first && isKnownTemplate) {
				List<FieldDefinition> typeFields = new ArrayList<>();
				
				builder
					.name(ROOT_CONTAINER_ID)
					.isDirectRootContainer(true);
				
				rootDefinition = ROOT_CONTAINER_ID;
			
				for (Type type : klass.getTypeParameters()) {
					ImmutableFieldDefinition.Builder fieldBuilder = ImmutableFieldDefinition.builder();
					fieldBuilder.name(type.getTypeName());
					
					String typeKlass = templateParams[typeFields.size()].getTypeName().split("<")[0];
					
					ExceptionUtils.withRuntimeException(()->{
						setupField(Class.forName(typeKlass), templateParams[typeFields.size()].getTypeName(), fieldBuilder, classesToBeDefined);
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
			fieldBuilder.valueType(ValueType.STRING);
		} else if (UUID.class.isAssignableFrom(klass)) {
			fieldBuilder.valueType(ValueType.STRING);
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
			fieldBuilder.valueType(ValueType.NUMBER);
		} else if (Enum.class.isAssignableFrom(klass)) {
			fieldBuilder.valueType(ValueType.ENUMERABLE);
			
			fieldBuilder.enumValues(Stream.of(klass.getEnumConstants()).map(o->((Enum<?>)o).name()).collect(Collectors.toList()));
		} else if (Boolean.class.isAssignableFrom(klass) || boolean.class.isAssignableFrom(klass)) {
			fieldBuilder.valueType(ValueType.BOOL);
		} else if (LocalDate.class.isAssignableFrom(klass)) {
			fieldBuilder.valueType(ValueType.DATE);
		} else if (LocalTime.class.isAssignableFrom(klass)) {
			fieldBuilder.valueType(ValueType.TIME);
		} else if (LocalDateTime.class.isAssignableFrom(klass)) {
			fieldBuilder.valueType(ValueType.DATETIME);
		} else if (Optional.class.isAssignableFrom(klass)) {
			fieldBuilder.containerType(ContainerType.OPTIONAL);
			
			fillTemplateParams(fieldBuilder, klass, returnTypeDesc, classesToBeDefined);
		} else if (Collection.class.isAssignableFrom(klass)) {
			fieldBuilder.containerType(ContainerType.LIST);
			
			fillTemplateParams(fieldBuilder, klass, returnTypeDesc, classesToBeDefined);
		} else if (Map.class.isAssignableFrom(klass)) {
			fieldBuilder.containerType(ContainerType.MAP);
			
			fillTemplateParams(fieldBuilder, klass, returnTypeDesc, classesToBeDefined);
		} else {
			if (!definitions.containsKey(klass.getName())) {
				classesToBeDefined.add(klass);
			}
			
			fieldBuilder
				.valueType(ValueType.REF)
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

	public void throwIfPathNotExists(TypePath path) {
		TypeDefinition actType = definitions.get(rootDefinition);
		FieldDefinition actField = null;
		
//		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//		System.out.println(path.getDebug());
		
		for (int i=0;i<path.getItems().size();++i) {
			TypePathItem item = path.getItems().get(i);

			
//			System.out.println("----------------"+item+"-----------------");
//			System.out.println(actType);
//			System.out.println(actField);
			
			try {
				Validate.oneNotNull(actType, actField);

				if (item.getType()==TypeSelectorType.KEEP_RECURSIVELY) {
					Validate.equals(i+1, path.getItems().size());
					return;
				}
				
				if (actType!=null && actType.getIsDirectRootContainer()) {
					Validate.isPresent(actType.getContainerType());
					
					if (actType.getContainerType().get()==ContainerType.OPTIONAL) {
						Validate.isPresent(actType.getTemplateParams());
						Validate.size(actType.getTemplateParams().get(), 1);
						
						actField = actType.getTemplateParams().get().get(0);
						actType = null;
					}
				}
				
				if (actType!=null) {
					if (actType.getIsDirectRootContainer()) {
						Validate.isPresent(actType.getContainerType());
						
						if (actType.getContainerType().get()==ContainerType.LIST) {
							Validate.equals(item.getType(), TypeSelectorType.INDEX);
							Validate.isPresent(actType.getTemplateParams());
							Validate.size(actType.getTemplateParams().get(), 1);
							
							actField = actType.getTemplateParams().get().get(0);
							actType = null;
						} else if (actType.getContainerType().get()==ContainerType.MAP) {
							Validate.equals(item.getType(), TypeSelectorType.KEY);
							Validate.isPresent(actType.getTemplateParams());
							Validate.size(actType.getTemplateParams().get(), 2);
							
							actField = actType.getTemplateParams().get().get(1);
							actType = null;
						} else {
							throw new RuntimeException(StringUtils.subst("Unexpected container type {}", actType.getContainerType().get()));
						}
					} else {
						Validate.equals(item.getType(), TypeSelectorType.FIELD);
						
						Optional<FieldDefinition> field = actType.getFields().stream().filter(f->f.getName().equals(item.getFieldName().get())).findFirst();
						
						Validate.isPresent(field);
						
						actField = field.get();
						actType = null;
					}			
				} else {
					while (actField.getContainerType().isPresent() && actField.getContainerType().get()==ContainerType.OPTIONAL) {
						Validate.isPresent(actField.getTemplateParams());
						Validate.size(actField.getTemplateParams().get(), 1);
						
						actField = actField.getTemplateParams().get().get(0);
					}
					
					Validate.isPresent(actField.getContainerType());
					
					if (actField.getContainerType().isPresent() && actField.getContainerType().get()==ContainerType.LIST) {
						Validate.equals(item.getType(), TypeSelectorType.INDEX);
						Validate.isPresent(actField.getTemplateParams());
						Validate.size(actField.getTemplateParams().get(), 1);
						
						actField = actField.getTemplateParams().get().get(0);
					} else if (actField.getContainerType().isPresent() && actField.getContainerType().get()==ContainerType.MAP) {
						Validate.equals(item.getType(), TypeSelectorType.KEY);
						Validate.isPresent(actField.getTemplateParams());
						Validate.size(actField.getTemplateParams().get(), 2);
						
						actField = actField.getTemplateParams().get().get(1);
					} else {
						throw new RuntimeException(StringUtils.subst("Unexpected container type {}", actField.getContainerType().get()));
					}
				}
				
				while (actField!=null && actField.getContainerType().isPresent() && actField.getContainerType().get()==ContainerType.OPTIONAL) {
					Validate.isPresent(actField.getTemplateParams());
					Validate.size(actField.getTemplateParams().get(), 1);
					
					actField = actField.getTemplateParams().get().get(0);
				}
				
				if (actField.getTypeRef().isPresent()) {
					actType = definitions.get(actField.getTypeRef().get());
					actField = null;
				}	
			} catch (Throwable t) {
				throw new RuntimeException(StringUtils.subst("Problem with path on item {} of schema {}", path.getDebug(i), JsonUtils.prettyGet(this)), t);
			}
		}
		
		Validate.isTrue(actField!=null && actType==null, "It is expected last item will select field");
		Validate.isTrue(actField.getValueType().isPresent(), "It seems path is too short and ends in collection, not field");
	}
}