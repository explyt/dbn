/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.execution.java.wrapper;

import com.dbn.execution.java.wrapper.JavaComplexType.AttributeDirection;
import com.dbn.object.DBJavaClass;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBJavaField;
import com.dbn.object.DBOrderedObject;
import com.dbn.object.DBJavaParameter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Comparator;

import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

/**
 * Parses {@link DBJavaMethod} instances into {@link Wrapper} objects,
 * including generating the corresponding {@link JavaComplexType}
 * and associated {@link SqlComplexType}.
 *
 * <p>This implementation uses a Singleton pattern and keeps
 * all per-parse mutable state in local variables inside
 * {@link #build(DBJavaMethod)} so that it is thread-safe
 * if multiple threads call it simultaneously.</p>
 */
@Slf4j
public final class WrapperBuilder {

	/**
	 * The single, static instance of WrapperBuilder.
	 */
	private static final WrapperBuilder INSTANCE = new WrapperBuilder();

	public static final String newSqlTypePrepend = "DBN_OJVM_TYPE_";

	/**
	 * Private constructor to enforce Singleton usage.
	 */
	private WrapperBuilder() {
		// no-op
	}

	public static WrapperBuilder getInstance() {
		return INSTANCE;
	}


	/**
	 * Entry point for parsing a {@link DBJavaMethod} into a {@link Wrapper}.
	 *
	 * @param dbJavaMethod The method definition to parse.
	 * @return The fully-populated {@link Wrapper}.
	 * @throws Exception if parsing fails or an unsupported cycle is detected.
	 */
	public Wrapper build(final DBJavaMethod dbJavaMethod) throws Exception {
		// Create data structures that are unique to *this* parse call.
		WrapperBuilderContext context = new WrapperBuilderContext();

		// Delegate to the internal parsing method.
		return buildInternal(dbJavaMethod, context);
	}

	// -------------------------------------------------
	// Internal Parsing Logic
	// -------------------------------------------------

	/**
	 * Internal method that actually performs the parsing to produce a {@link Wrapper}.
	 *
	 * @param dbJavaMethod         The method definition to parse.
	 * @param context contains the following
	 *  complexTypeConversion Mapping from className -> unique integer (for type naming).
	 *  complexTypeMap        Cache of complex types created so far during this parse.
	 *  complexTypeSet        Used to detect cycles during recursive type creation.
	 * @return The generated {@link Wrapper}.
	 */
	private Wrapper buildInternal(
			final DBJavaMethod dbJavaMethod,
			WrapperBuilderContext context
	) {
		// Create a fresh Wrapper for this invocation
		Wrapper wrapper = new Wrapper();

		setMethodMetadata(dbJavaMethod, wrapper);
		parseParameters(dbJavaMethod, wrapper, context);
		parseReturnType(dbJavaMethod, wrapper, context);

		return wrapper;
	}

	/**
	 * Sets up the basic method metadata on the {@link Wrapper} object.
	 */
	private void setMethodMetadata(DBJavaMethod dbJavaMethod, Wrapper wrapper) {
		final String methodName = dbJavaMethod.getName().split("#")[0];
		wrapper.setWrappedJavaMethodName(methodName);
		wrapper.setFullyQualifiedClassName(convertClassNameToDotNotation(dbJavaMethod.getClassName()));
		// Replace "void" return in the signature with a more readable style, if present.
		wrapper.setJavaMethodSignature(
				dbJavaMethod.getSignature().replace(": void", "").replace(":", " return")
		);
	}

	/**
	 * Parse all method parameters from the given {@link DBJavaMethod} and populate the wrapper.
	 */
	private void parseParameters(
			final DBJavaMethod dbJavaMethod,
			Wrapper wrapper,
			WrapperBuilderContext context
	) {
		final List<DBJavaParameter> dbJavaMethodParameters = dbJavaMethod.getParameters();
		if (dbJavaMethodParameters == null || dbJavaMethodParameters.isEmpty()) {
			return;
		}

		// Sort by position to ensure correct order
		dbJavaMethodParameters.sort(Comparator.comparingInt(DBOrderedObject::getPosition));

		// Create a Wrapper.MethodAttribute for each parameter
		for (DBJavaParameter parameter : dbJavaMethodParameters) {
			final DBJavaClass parameterClass = parameter.getParameterClass();
			final String className = (parameterClass == null) ? "" : parameterClass.getQualifiedName();

			Wrapper.MethodAttribute attr = createMethodAttribute(
					parameterClass,
					parameter.getParameterType(),
					className,
					parameter.getArrayDepth(),
					AttributeDirection.ARGUMENT,
					context,
					wrapper
			);
			wrapper.addMethodArgument(attr);
		}
	}

	/**
	 * Parse the return type (if not void) and populate the wrapper.
	 */
	private void parseReturnType(
			DBJavaMethod dbJavaMethod,
			Wrapper wrapper,
			WrapperBuilderContext context
	) {
		if (!"void".equals(dbJavaMethod.getReturnType())) {
			Wrapper.MethodAttribute returnAttr = createMethodAttribute(
					dbJavaMethod.getReturnClass(),
					dbJavaMethod.getReturnType(),
					dbJavaMethod.getClassName(),
					dbJavaMethod.getArrayDepth(),
					AttributeDirection.RETURN,
					context,
					wrapper
			);
			wrapper.setReturnType(returnAttr);
		}
	}

	/**
	 * Creates a {@link Wrapper.MethodAttribute} for the given DB elements, either
	 * a simple attribute if primitive/supported, or a complex type otherwise.
	 */
	private Wrapper.MethodAttribute createMethodAttribute(
			final DBJavaClass dbJavaClass,
			String type,
			String className,
			short arrayDepth,
			AttributeDirection attributeDirection,
			WrapperBuilderContext context,
			Wrapper wrapper
	) {
		final String effectiveParameterType = getParameterType(type, className);

		// If non-array and we have a direct mapping -> simple attribute
		if (arrayDepth == 0 && TypeMappingsManager.isSupportedType(effectiveParameterType)) {
			return buildSimpleMethodAttribute(effectiveParameterType);
		}

		// Otherwise, build or retrieve a JavaComplexType
		final JavaComplexType javaComplexType = (arrayDepth > 0)
				? createJavaComplexArrayType(dbJavaClass, effectiveParameterType, arrayDepth, attributeDirection,
				context, wrapper)
				: createJavaComplexType(dbJavaClass, effectiveParameterType, attributeDirection,
				context, wrapper);

		if (javaComplexType == null) {
			// If still null, it's unsupported or cyclical
			return null;
		}

		// Build a complex attribute
		return buildComplexMethodAttribute(javaComplexType);
	}

	/**
	 * Builds a simple (non-complex) method attribute with a known SQL type mapping.
	 */
	private Wrapper.MethodAttribute buildSimpleMethodAttribute(String effectiveParameterType) {
		Wrapper.MethodAttribute methodAttribute = new Wrapper.MethodAttribute();
		methodAttribute.setArray(false);
		methodAttribute.setTypeName(convertClassNameToDotNotation(effectiveParameterType));
		methodAttribute.setCorrespondingSqlTypeName(
				TypeMappingsManager.getCorrespondingSqlType(effectiveParameterType).getSqlTypeName()
		);
		methodAttribute.setComplexType(false);
		return methodAttribute;
	}

	/**
	 * Builds a method attribute that is backed by a {@link JavaComplexType}.
	 */
	private Wrapper.MethodAttribute buildComplexMethodAttribute(JavaComplexType javaComplexType) {
		Wrapper.MethodAttribute methodAttribute = new Wrapper.MethodAttribute();
		methodAttribute.setArray(javaComplexType.isArray());
		methodAttribute.setArrayDepth(javaComplexType.getArrayDepth());
		methodAttribute.setTypeName(convertClassNameToDotNotation(javaComplexType.getTypeName()));
		methodAttribute.setComplexType(true);

		SqlComplexType sqlType = javaComplexType.getCorrespondingSqlType();
		methodAttribute.setCorrespondingSqlTypeName((sqlType == null) ? "" : sqlType.getName());

		return methodAttribute;
	}

	// -------------------------------------------------
	// Complex Type Creation Logic
	// -------------------------------------------------

	/**
	 * Creates a {@link JavaComplexType} (non-array version) for the given class
	 * or parameter type, populating its fields recursively if needed.
	 */
	private JavaComplexType createJavaComplexType(
			final DBJavaClass dbJavaClass,
			final String parameterType,
			final AttributeDirection attributeDirection,
			WrapperBuilderContext context,
			Wrapper wrapper
	) {
		final ComplexTypeKey key = buildComplexTypeKey(dbJavaClass, parameterType, (short) 0);
		if (addToContextAndDetectCycle(key, context)) return null;

		JavaComplexType existing = getComplexTypeFromCache(key, attributeDirection, context);
		if (existing != null) {
			context.removeFromSet(key);
			return existing;
		}

		// Create a new complex type shell
		JavaComplexType javaComplexType = buildComplexTypeShell(dbJavaClass, parameterType, attributeDirection, false);
		SqlComplexType sqlComplexType = new SqlComplexType();
		sqlComplexType.setArray(false);

		// Populate fields if we have a DBJavaClass
		if (dbJavaClass != null) {
			populateComplexTypeFields(dbJavaClass, attributeDirection, javaComplexType,
					sqlComplexType, context, wrapper);
		}

		// Finalize and return
		finalizeComplexType(key, javaComplexType, sqlComplexType, context, wrapper);
		return javaComplexType;
	}

	/**
	 * Creates a {@link JavaComplexType} for array types (including nested arrays),
	 * populating its contained type recursively if necessary.
	 */
	private JavaComplexType createJavaComplexArrayType(
			final DBJavaClass dbJavaClass,
			final String parameterType,
			final short arrayDepth,
			final AttributeDirection attributeDirection,
			WrapperBuilderContext context,
			Wrapper wrapper
	) {
		final ComplexTypeKey key = buildComplexTypeKey(dbJavaClass, parameterType, arrayDepth);
		if (addToContextAndDetectCycle(key, context)) return null;

		JavaComplexType existing = getComplexTypeFromCache(key, attributeDirection, context);
		if (existing != null) {
			context.removeFromSet(key);
			return existing;
		}

		// Create new array-type shell
		JavaComplexType javaComplexType = buildComplexTypeShell(dbJavaClass, parameterType, attributeDirection, true);
		javaComplexType.setArrayDepth(arrayDepth);

		SqlComplexType sqlComplexType = new SqlComplexType();
		sqlComplexType.setArray(true);

		// If base type is unsupported, abort
		if (TypeMappingsManager.isUnSupportedType(parameterType)) {
			log.error("Encountered unsupported type for array: {}", parameterType);
			context.removeFromSet(key);
			return null;
		}

		// Identify the contained type name
		final String sqlBaseName = (dbJavaClass != null) ? dbJavaClass.getName() : parameterType;

		String containedTypeName = null;
		JavaComplexType containedJavaComplexType;

		// Single-dimension vs multi-dimension array
		if (arrayDepth <= 1) {
			containedTypeName = getContainedTypeName(parameterType);
			if (containedTypeName == null) {
				// Possibly a nested complex type
				containedJavaComplexType = createJavaComplexType(dbJavaClass, parameterType, attributeDirection,
						context, wrapper);
				if (containedJavaComplexType != null) {
					containedTypeName = containedJavaComplexType.getCorrespondingSqlType().getName();
				}
			}
		} else {
			// Multi-dimensional
			containedJavaComplexType = createJavaComplexArrayType(dbJavaClass, parameterType,
					(short) (arrayDepth - 1), attributeDirection,
					context, wrapper);
			if (containedJavaComplexType != null) {
				containedTypeName = containedJavaComplexType.getCorrespondingSqlType().getName();
			}
		}

		sqlComplexType.setContainedTypeName(containedTypeName);
		sqlComplexType.setName(getOrCreateNewSqlTypeName(sqlBaseName, arrayDepth, wrapper));
		javaComplexType.setCorrespondingSqlType(sqlComplexType);

		wrapper.addArgumentJavaComplexType(javaComplexType);
		context.addMapEntry(key, javaComplexType);
		context.removeFromSet(key);

		return javaComplexType;
	}

	/**
	 * Builds a fresh {@link JavaComplexType} shell (for both array and non-array types).
	 */
	private JavaComplexType buildComplexTypeShell(DBJavaClass dbJavaClass, String parameterType, AttributeDirection attributeDirection, boolean isArray) {
		JavaComplexType javaComplexType = new JavaComplexType();
		javaComplexType.setAttributeDirection(attributeDirection);
		javaComplexType.setArray(isArray);
		javaComplexType.setArrayType(isArray ? JavaComplexType.ArrayType.SQUARE_BRACKET : null);
		javaComplexType.setArrayDepth((short) 0);
		javaComplexType.setTypeName(parameterType);

		if (dbJavaClass != null) {
			javaComplexType.setTypeName(dbJavaClass.getName());
		}
		return javaComplexType;
	}

	/**
	 * Attempts to return a contained (base) type name if it is a direct mapping in TypeMappingsManager.
	 * Returns {@code null} if no direct mapping is found (indicating a complex type).
	 */
	private String getContainedTypeName(String parameterType) {
		if (TypeMappingsManager.isSupportedType(parameterType)) {
			return TypeMappingsManager.getCorrespondingSqlType(parameterType).getSqlTypeName();
		}
		return null;
	}

	// -------------------------------------------------
	// ComplexTypeKey and Cycle Detection
	// -------------------------------------------------

	/**
	 * Builds a new {@link ComplexTypeKey} from the given parameters.
	 */
	private ComplexTypeKey buildComplexTypeKey(DBJavaClass dbJavaClass, String parameterType, short arrayDepth) {
		String keyName = (dbJavaClass == null)
				? convertClassNameToDotNotation(parameterType)
				: convertClassNameToDotNotation(dbJavaClass.getQualifiedName());
		return new ComplexTypeKey(keyName, arrayDepth);
	}

	/**
	 * Checks if we have already encountered this key, indicating a cycle.
	 */
	private boolean addToContextAndDetectCycle(ComplexTypeKey key, WrapperBuilderContext context) {
		if (context.detectRepetition(key)) {
			log.error("Encountered cycle for key: {}", key);
			return true;
		}
		context.addToSet(key);
		return false;
	}

	// -------------------------------------------------
	// Caching and Direction Upgrades
	// -------------------------------------------------

	/**
	 * Returns a cached {@link JavaComplexType} if present, and upgrades its direction
	 * from ARGUMENT to BOTH if needed (when also used as a RETURN).
	 */
	private JavaComplexType getComplexTypeFromCache(
			ComplexTypeKey key,
			AttributeDirection direction,
			WrapperBuilderContext context
	) {
		JavaComplexType javaComplexType = context.getJavaComplexType(key);
		if (javaComplexType != null) {
			// If it was ARGUMENT-only, and now we need a RETURN, upgrade to BOTH
			if (direction == AttributeDirection.RETURN
					&& javaComplexType.getAttributeDirection() == AttributeDirection.ARGUMENT) {
				changeAttributeDirection(javaComplexType, context);
			}
		}
		return javaComplexType;
	}

	/**
	 * If a {@link JavaComplexType} was first encountered as an ARGUMENT but is also needed
	 * for RETURN, we mark it (and nested fields) as BOTH.
	 */
	private void changeAttributeDirection(
			JavaComplexType javaComplexType,
			WrapperBuilderContext context
	) {
		javaComplexType.setAttributeDirection(AttributeDirection.BOTH);

		if (javaComplexType.isArray()) {
			// For arrays, mark all corresponding array dimension entries + the base
			for (short i = 1; i <= javaComplexType.getArrayDepth(); i++) {
				final ComplexTypeKey complexTypeKey = new ComplexTypeKey(
						javaComplexType.getTypeName(), javaComplexType.getArrayDepth()
				);
				JavaComplexType mappedType = context.getJavaComplexType(complexTypeKey);
				if (mappedType != null) {
					mappedType.setAttributeDirection(AttributeDirection.BOTH);
				}
			}
			// Also update the non-array variant if it exists
			final ComplexTypeKey baseKey = new ComplexTypeKey(javaComplexType.getTypeName(), (short) 0);
			JavaComplexType baseType = context.getJavaComplexType(baseKey);
			if (baseType != null) {
				changeAttributeDirection(baseType, context);
			}
		} else {
			// For complex objects, recursively mark subfields
			for (JavaComplexType.Field field : javaComplexType.getFields()) {
				if (field.isComplexType()) {
					final ComplexTypeKey complexTypeKey = new ComplexTypeKey(
							field.getType(), field.getArrayDepth()
					);
					JavaComplexType nested = context.getJavaComplexType(complexTypeKey);
					if (nested != null) {
						changeAttributeDirection(nested, context);
					}
				}
			}
		}
	}

	// -------------------------------------------------
	// Finalizing Complex Types
	// -------------------------------------------------

	/**
	 * Finalizes a newly constructed {@link JavaComplexType}, storing it in the cache
	 * and linking its corresponding SQL type.
	 */
	private void finalizeComplexType(
			ComplexTypeKey key,
			JavaComplexType javaComplexType,
			SqlComplexType sqlComplexType,
			WrapperBuilderContext context,
			Wrapper wrapper
	) {
		sqlComplexType.setName(getOrCreateNewSqlTypeName(javaComplexType.getTypeName()
				, javaComplexType.getArrayDepth(), wrapper));
		javaComplexType.setCorrespondingSqlType(sqlComplexType);
		wrapper.addArgumentJavaComplexType(javaComplexType);

		context.addMapEntry(key, javaComplexType);
		context.removeFromSet(key);
	}

	// -------------------------------------------------
	// Field Population
	// -------------------------------------------------

	/**
	 * Populates the fields of a {@link JavaComplexType} given a {@link DBJavaClass},
	 * building nested types if necessary.
	 */
	private void populateComplexTypeFields(
			DBJavaClass dbJavaClass,
			AttributeDirection attributeDirection,
			JavaComplexType javaComplexType,
			SqlComplexType sqlComplexType,
			WrapperBuilderContext context,
			Wrapper wrapper
	) {
		List<DBJavaField> dbJavaFields = dbJavaClass.getFields();
		for (DBJavaField dbJavaField : dbJavaFields) {
			JavaComplexType.Field field = buildJavaComplexField(dbJavaField, dbJavaClass, wrapper);
			javaComplexType.addField(field);

			// If it's a primitive or directly supported type, add to the SQL type
			final SqlType sqlType = TypeMappingsManager.getCorrespondingSqlType(field.getType());
			if (sqlType != null && dbJavaField.getArrayDepth() <= 0) {
				sqlComplexType.addField(field.getName(), sqlType.getSqlTypeName(), field.getFieldIndex());
			} else {
				// It's a nested complex field
				handleNestedField(field, dbJavaField, attributeDirection, sqlComplexType,
						context, wrapper);
			}
		}
	}

	/**
	 * Builds a single {@link JavaComplexType.Field} instance from a {@link DBJavaField}.
	 */
	private JavaComplexType.Field buildJavaComplexField(DBJavaField dbJavaField, DBJavaClass dbJavaClass, Wrapper wrapper) {
		JavaComplexType.Field field = new JavaComplexType.Field();

		// Get the raw field type in string form
		String fieldParameter;
		try {
			fieldParameter = getParameterType(dbJavaField.getType(), dbJavaField.getClassName());
		} catch (Exception e) {
			log.error("Could not create JavaComplexType for field: {}", dbJavaField, e);
			conditionallyLog(e);
			fieldParameter = dbJavaField.getType(); // fallback
		}

		if (TypeMappingsManager.isUnSupportedType(fieldParameter)) {
			log.error("Encountered unsupported type for field {}: {}", dbJavaField, fieldParameter);
		}

		// Basic field setup
		field.setFieldIndex(dbJavaField.getIndex());
		field.setName(dbJavaField.getName());
		field.setAccessModifier(dbJavaField.getAccessibility().toString());
		field.setType(fieldParameter, TypeMappingsManager.getCorrespondingSqlType(fieldParameter));

		// If array
		if (dbJavaField.getArrayDepth() > 0) {
			field.setArray(true);
			field.setArrayDepth(dbJavaField.getArrayDepth());
		}

		// If the field is non-public, set up the getter/setter if present
		if (!"PUBLIC".equals(field.getAccessModifier().toString())) {
			field.setGetter(getGetter(field.getName(), fieldParameter, field.getArrayDepth(), dbJavaClass));
			field.setSetter(getSetter(field.getName(), fieldParameter, field.getArrayDepth(), dbJavaClass));
		}

		// If the underlying Java class is known
		if (field.getSqlType().isEmpty()) {
			// Re-use the same complexTypeConversion map.
			field.setSqlType(getOrCreateNewSqlTypeName(fieldParameter, field.getArrayDepth(), wrapper));
		}

		return field;
	}

	/**
	 * Handles a nested field that either references a nested array type or a nested complex type.
	 */
	private void handleNestedField(
			JavaComplexType.Field field,
			DBJavaField dbJavaField,
			AttributeDirection attributeDirection,
			SqlComplexType sqlComplexType,
			WrapperBuilderContext context,
			Wrapper wrapper
	) {
		field.setComplexType(true);
		JavaComplexType fieldJavaComplexType;
		if (dbJavaField.getArrayDepth() > 0) {
			// Nested array
			fieldJavaComplexType = createJavaComplexArrayType(
					dbJavaField.getFieldClass(),
					field.getType(),
					dbJavaField.getArrayDepth(),
					attributeDirection,
					context,
					wrapper
			);
		} else {
			// Nested object
			fieldJavaComplexType = createJavaComplexType(
					dbJavaField.getFieldClass(),
					field.getType(),
					attributeDirection,
					context,
					wrapper
			);
		}
		if (fieldJavaComplexType != null) {
			sqlComplexType.addField(
					field.getName(),
					fieldJavaComplexType.getCorrespondingSqlType().getName(),
					field.getFieldIndex()
			);
			field.setSqlType(sqlComplexType.getName());
		}
	}

	// -------------------------------------------------
	// Getters / Setters
	// -------------------------------------------------

	/**
	 * Finds a setter method matching the given field signature, if one exists.
	 */
	private String getSetter(
			final String fieldName,
			final String fieldParameter,
			final short arrayDepth,
			final DBJavaClass dbJavaClass
	) {
		if (dbJavaClass == null) return null;

		final String setterName = "set" + capitalize(fieldName);
		List<DBJavaMethod> methods = dbJavaClass.getMethods();
		DBJavaMethod methodSet = null;
		for (DBJavaMethod method : methods) {
			if (setterName.equals(method.getName().split("#")[0])) {
				methodSet = method;
				break;
			}
		}

		if (methodSet != null) {
			List<DBJavaParameter> setMethodParameters = methodSet.getParameters();
			if (setMethodParameters.size() == 1) {
				try {
					DBJavaParameter param = setMethodParameters.get(0);
					String targetFieldClass;
					if (dbJavaClass.getField(fieldName) != null
							&& dbJavaClass.getField(fieldName).getFieldClass() != null) {
						targetFieldClass = convertClassNameToDotNotation(
								dbJavaClass.getField(fieldName).getFieldClass().getQualifiedName()
						);
					} else {
						targetFieldClass = fieldParameter;
					}
					if (getParameterType(param).equals(targetFieldClass)
							&& param.getArrayDepth() == arrayDepth) {
						return methodSet.getName().split("#")[0];
					}
				} catch (Exception e) {
					log.error("Could not get Setter method for Field {} in class {}", fieldName, dbJavaClass, e);
					conditionallyLog(e);
				}
			}
		}
		return null;
	}

	/**
	 * Finds a getter method matching the given field signature, if one exists.
	 */
	private String getGetter(
			final String fieldName,
			final String fieldParameter,
			final short arrayDepth,
			final DBJavaClass dbJavaClass
	) {
		if (dbJavaClass == null) return null;

		final String getterName = "get" + capitalize(fieldName);
		List<DBJavaMethod> methods = dbJavaClass.getMethods();
		DBJavaMethod methodGet = null;
		for (DBJavaMethod method : methods) {
			if (getterName.equals(method.getName().split("#")[0])) {
				methodGet = method;
				break;
			}
		}

		if (methodGet != null) {
			try {
				final String methodReturn = getParameterType(methodGet.getReturnType(), methodGet.getClassName());
				if (methodReturn.equals(fieldParameter)
						&& methodGet.getArrayDepth() == arrayDepth
						&& methodGet.getParameters().isEmpty()) {
					return methodGet.getName().split("#")[0];
				}
			} catch (Exception e) {
				log.error("Could not get Getter method for Field {} in class {}", fieldName, dbJavaClass, e);
				conditionallyLog(e);
			}
		}
		return null;
	}

	// -------------------------------------------------
	// Type Utilities
	// -------------------------------------------------

	/**
	 * Retrieves the parameter type from a {@link DBJavaParameter}.
	 */
	private String getParameterType(final DBJavaParameter dbJavaMethodParameter) {
		String parameterType = convertClassNameToDotNotation(dbJavaMethodParameter.getParameterType());

		// If parameter type is empty, try to get it from the parameter class
		if (parameterType.isEmpty() || parameterType.equals("-") || parameterType.equals("class")) {
			final DBJavaClass parameterClass = dbJavaMethodParameter.getParameterClass();
			if (parameterClass != null) {
				parameterType = convertClassNameToDotNotation(parameterClass.getQualifiedName());
			}
		}
		validateParameterType(parameterType);
		return parameterType;
	}

	/**
	 * Retrieves the parameter type by analyzing a raw type and class name, if needed.
	 */
	private String getParameterType(final String type, final String className) {
		String parameterType = convertClassNameToDotNotation(type);

		if (parameterType.isEmpty() || parameterType.equals("-") || parameterType.equals("class")) {
			if (className != null) {
				parameterType = convertClassNameToDotNotation(className);
			}
		}
		validateParameterType(parameterType);
		return parameterType;
	}

	/**
	 * Builds a new SQL type name, prepending a constant prefix plus an incrementing integer.
	 */
	private String getOrCreateNewSqlTypeName(final String className, final short arrayDepth, Wrapper wrapper) {
		// The SQL type prefix used to create new type names
		ComplexTypeKey key = new ComplexTypeKey(className, arrayDepth);
		if (!wrapper.getComplexTypeConversion().containsKey(key)) {
			// Assign a new index based on current map size (or use another scheme if needed)
			wrapper.addEntryToComplexTypeConversion(key,
					wrapper.getComplexTypeConversion().size() + 1);
		}
		return newSqlTypePrepend + wrapper.getComplexTypeConversion().get(key);
	}

	/**
	 * Ensures the parameter type is neither {@code null} nor empty. Logs an error if it is.
	 */
	private void validateParameterType(final String parameterType) {
		if (parameterType == null || parameterType.isEmpty()) {
			log.error("Parameter type is empty or null.");
		}
	}

	/**
	 * Converts slash notation to dot notation (e.g., "java/lang/String" -> "java.lang.String").
	 */
	public static String convertClassNameToDotNotation(final String className) {
		return className.replace('/', '.');
	}

	/**
	 * Capitalizes the first character of a string (e.g. "field" -> "Field").
	 */
	private String capitalize(final String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	// -------------------------------------------------
	// ComplexTypeKey
	// -------------------------------------------------

	/**
	 * A composite key of {@code className + arrayLength} used to ensure uniqueness
	 * when creating/looking up complex types. Prevents collisions for arrays
	 * of the same class at different depths.
	 */
	public static class ComplexTypeKey {
		private final String className;
		private final short arrayLength;

		public ComplexTypeKey(final String className, final short arrayLength) {
			this.className = className;
			this.arrayLength = arrayLength;
		}

		// Required for proper usage in Maps/Sets
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ComplexTypeKey)) return false;
			ComplexTypeKey that = (ComplexTypeKey) o;
			return arrayLength == that.arrayLength && className.equals(that.className);
		}

		@Override
		public int hashCode() {
			int result = className.hashCode();
			result = 31 * result + arrayLength;
			return result;
		}

		@Override
		public String toString() {
			return "ComplexTypeKey{className='" + className + "', arrayLength=" + arrayLength + '}';
		}
	}
}
