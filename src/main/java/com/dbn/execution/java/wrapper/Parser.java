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
import com.intellij.openapi.project.ProjectManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Comparator;

import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

/**
 * Parses {@link DBJavaMethod} instances into {@link Wrapper} objects, including generating
 * the corresponding {@link JavaComplexType} and associated {@link SqlComplexType}.
 */
@Slf4j
public class Parser {

	int newSQLTypeCounter = 1;
	/**
	 * The SQL type prefix used to create new type names.
	 */
	public static final String newSqlTypePrepend = "DBN_OJVM_TYPE_";

	/**
	 * Map to store type number for each complex java class.
	 * Point -> TYPE_1
	 */
	private Map<String, Integer> complexTypeConversion = new HashMap<>();

	/**
	 * Holds cached mappings between a composite key (className + arrayDepth) and
	 * its corresponding {@link JavaComplexType}.
	 */
	private final Map<ComplexTypeKey, JavaComplexType> complexTypeMap = new HashMap<>();

	/**
	 * Holds composite keys currently in process, to detect and prevent recursion cycles.
	 */
	private final Set<ComplexTypeKey> complexTypeSet = new HashSet<>();

	/**
	 * Supported mappings of Java type -> SQL type, provided by the {@link TypeMappingsManager}.
	 */
	private final Map<String, SqlType> supportedTypes = TypeMappingsManager.getInstance(ProjectManager.getInstance().getDefaultProject()).getDataTypeMap();

	/**
	 * Set of unsupported type names.
	 */
	private final Set<String> unsupportedTypes = TypeMappingsManager.getInstance(ProjectManager.getInstance().getDefaultProject()).getUnsupportedTypes();

	/**
	 * The method currently being parsed.
	 */
	private DBJavaMethod dbJavaMethod;

	/**
	 * The generated wrapper for the given method.
	 */
	private Wrapper wrapper;

	/**
	 * Entry point for parsing a {@link DBJavaMethod} into a {@link Wrapper}.
	 *
	 * @param dbJavaMethod The method definition to parse.
	 * @return The fully-populated {@link Wrapper}.
	 * @throws Exception if parsing fails or an unsupported cycle is detected.
	 */
	public Wrapper parse(final DBJavaMethod dbJavaMethod) throws Exception {
		this.dbJavaMethod = dbJavaMethod;
		return parseInternal();
	}

	/**
	 * Internal method that actually performs the parsing to produce a {@link Wrapper}.
	 *
	 * @return The generated {@link Wrapper}.
	 */
	private Wrapper parseInternal() {
		wrapper = new Wrapper();

		setMethodMetadata();
		parseParameters();
		parseReturnType();
		wrapper.setComplexTypeConversion(this.complexTypeConversion);
		return wrapper;
	}

	/**
	 * Sets up the basic method metadata on the {@link Wrapper} object.
	 */
	private void setMethodMetadata() {
		final String methodName = dbJavaMethod.getName().split("#")[0];
		wrapper.setWrappedJavaMethodName(methodName);
		wrapper.setFullyQualifiedClassName(convertClassNameToDotNotation(dbJavaMethod.getClassName()));
		// Replace "void" return in the signature with a more readable style, if present.
		wrapper.setJavaMethodSignature(dbJavaMethod.getSignature().replace(": void", "").replace(":", " return"));
	}

	/**
	 * Parse all method parameters from the current {@link DBJavaMethod} and populate the wrapper.
	 */
	private void parseParameters() {
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

			Wrapper.MethodAttribute attr = createMethodAttribute(parameterClass, parameter.getParameterType(), className, parameter.getArrayDepth(), AttributeDirection.ARGUMENT);
			wrapper.addMethodArgument(attr);
		}
	}

	/**
	 * Parse the return type (if not void) and populate the wrapper.
	 */
	private void parseReturnType() {
		if (!"void".equals(dbJavaMethod.getReturnType())) {
			Wrapper.MethodAttribute returnAttr = createMethodAttribute(dbJavaMethod.getReturnClass(), dbJavaMethod.getReturnType(), dbJavaMethod.getClassName(), dbJavaMethod.getArrayDepth(), AttributeDirection.RETURN);
			wrapper.setReturnType(returnAttr);
		}
	}

	/**
	 * Creates a {@link Wrapper.MethodAttribute} for the given DB elements, either
	 * a simple attribute if primitive/supported, or a complex type otherwise.
	 *
	 * @param dbJavaClass        The DB Java class, may be {@code null}.
	 * @param type               The raw type name.
	 * @param className          Qualified class name if available.
	 * @param arrayDepth         Number of array dimensions.
	 * @param attributeDirection Whether this is a RETURN or ARGUMENT.
	 * @return The created {@link Wrapper.MethodAttribute}, or {@code null} if unsupported.
	 */
	private Wrapper.MethodAttribute createMethodAttribute(final DBJavaClass dbJavaClass, final String type, final String className, final short arrayDepth, final AttributeDirection attributeDirection) {
		final String effectiveParameterType = getParameterType(type, className);

		// If non-array and we have a direct mapping -> simple attribute
		if (arrayDepth == 0 && supportedTypes.containsKey(effectiveParameterType)) {
			return buildSimpleMethodAttribute(effectiveParameterType);
		}

		// Otherwise, build or retrieve a JavaComplexType
		final JavaComplexType javaComplexType = (arrayDepth > 0) ? createJavaComplexArrayType(dbJavaClass, effectiveParameterType, arrayDepth, attributeDirection) : createJavaComplexType(dbJavaClass, effectiveParameterType, attributeDirection);

		// If still null, it's unsupported
		if (javaComplexType == null) {
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
		methodAttribute.setCorrespondingSqlTypeName(supportedTypes.get(effectiveParameterType).getSqlTypeName());
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

	/**
	 * Creates a {@link JavaComplexType} (non-array version) for the given class
	 * or parameter type, populating its fields recursively if needed.
	 *
	 * @param dbJavaClass        The DB Java class, may be {@code null}.
	 * @param parameterType      The parameter type (in string form).
	 * @param attributeDirection Whether this is a return type or argument.
	 * @return The created {@link JavaComplexType}, or {@code null} if a cycle or unsupported type is encountered.
	 */
	private JavaComplexType createJavaComplexType(final DBJavaClass dbJavaClass, final String parameterType, final AttributeDirection attributeDirection) {
		// Build unique key and detect cycles
		final ComplexTypeKey key = buildComplexTypeKey(dbJavaClass, parameterType, (short) 0);
		if (detectCycle(key)) return null;

		// Possibly retrieve from cache
		JavaComplexType existing = getComplexTypeFromCache(key, attributeDirection);
		if (existing != null) {
			complexTypeSet.remove(key);
			return existing;
		}

		// Create a new complex type shell
		JavaComplexType javaComplexType = buildComplexTypeShell(dbJavaClass, parameterType, attributeDirection, false);
		SqlComplexType sqlComplexType = new SqlComplexType();
		sqlComplexType.setArray(false);

		// Populate fields if we have a DBJavaClass
		if (dbJavaClass != null) {
			populateComplexTypeFields(dbJavaClass, attributeDirection, javaComplexType, sqlComplexType);
		}

		finalizeComplexType(key, javaComplexType, sqlComplexType);
		return javaComplexType;
	}

	/**
	 * Creates a {@link JavaComplexType} for array types (including nested arrays),
	 * populating its contained type recursively if necessary.
	 *
	 * @param dbJavaClass        The DB Java class, may be {@code null}.
	 * @param parameterType      The raw parameter type name.
	 * @param arrayDepth         How many array levels exist.
	 * @param attributeDirection Whether this is a return or argument type.
	 * @return The created {@link JavaComplexType}, or {@code null} if unsupported or a cycle is found.
	 */
	private JavaComplexType createJavaComplexArrayType(final DBJavaClass dbJavaClass, final String parameterType, final short arrayDepth, final AttributeDirection attributeDirection) {
		// Build unique key and detect cycles
		final ComplexTypeKey key = buildComplexTypeKey(dbJavaClass, parameterType, arrayDepth);
		if (detectCycle(key)) return null;

		// Possibly retrieve from cache
		JavaComplexType existing = getComplexTypeFromCache(key, attributeDirection);
		if (existing != null) {
			complexTypeSet.remove(key);
			return existing;
		}

		// Create new array-type shell
		JavaComplexType javaComplexType = buildComplexTypeShell(dbJavaClass, parameterType, attributeDirection, true);
		javaComplexType.setArrayDepth(arrayDepth);

		SqlComplexType sqlComplexType = new SqlComplexType();
		sqlComplexType.setArray(true);

		// If base type is unsupported, abort
		if (unsupportedTypes.contains(parameterType)) {
			log.error("Encountered unsupported type for array: {}", parameterType);
			complexTypeSet.remove(key);
			return null;
		}

		// Identify the contained type name
		final String sqlBaseName = (dbJavaClass != null) ? dbJavaClass.getName() : parameterType;
		final String sqlTypeNameAppend = buildArraySuffix(arrayDepth);

		// Recursively build or retrieve contained type
		String containedTypeName = null;
		JavaComplexType containedJavaComplexType;

		if (arrayDepth <= 1) {
			// Single-dimension array
			containedTypeName = getContainedTypeName(parameterType);
			if (containedTypeName == null) {
				// Possibly a nested complex type
				containedJavaComplexType = createJavaComplexType(dbJavaClass, parameterType, attributeDirection);
				if (containedJavaComplexType != null) {
					containedTypeName = containedJavaComplexType.getCorrespondingSqlType().getName();
				}
			}
		} else {
			// Multi-dimensional array
			containedJavaComplexType = createJavaComplexArrayType(dbJavaClass, parameterType, (short) (arrayDepth - 1), attributeDirection);
			if (containedJavaComplexType != null) {
				containedTypeName = containedJavaComplexType.getCorrespondingSqlType().getName();
			}
		}

		sqlComplexType.setContainedTypeName(containedTypeName);
		sqlComplexType.setName(getNewSqlTypeName(sqlBaseName + sqlTypeNameAppend));
		javaComplexType.setCorrespondingSqlType(sqlComplexType);

		wrapper.addArgumentJavaComplexType(javaComplexType);
		complexTypeMap.put(key, javaComplexType);
		complexTypeSet.remove(key);
		return javaComplexType;
	}

	/**
	 * Helper to build the suffix for multi-dimensional arrays (e.g. "ARRAY_2", "ARRAY_3"...).
	 */
	private String buildArraySuffix(short arrayDepth) {
		// If arrayDepth > 1, we’ll append _2, _3, etc.
		// If arrayDepth == 1, just "ARRAY".
		return (arrayDepth > 1) ? "ARRAY_" + arrayDepth : "ARRAY";
	}

	/**
	 * Attempts to return a contained (base) type name if it is a direct mapping in {@link #supportedTypes}.
	 * Returns {@code null} if no direct mapping is found (indicating a complex type).
	 */
	private String getContainedTypeName(String parameterType) {
		if (supportedTypes.containsKey(parameterType)) {
			return supportedTypes.get(parameterType).getSqlTypeName();
		}
		return null;
	}

	/**
	 * Constructs a new {@link ComplexTypeKey} from the given parameters.
	 */
	private ComplexTypeKey buildComplexTypeKey(DBJavaClass dbJavaClass, String parameterType, short arrayDepth) {
		String keyName = (dbJavaClass == null) ? convertClassNameToDotNotation(parameterType) : convertClassNameToDotNotation(dbJavaClass.getQualifiedName());
		return new ComplexTypeKey(keyName, arrayDepth);
	}

	/**
	 * Checks if we have already encountered this key, indicating a cycle.
	 */
	private boolean detectCycle(ComplexTypeKey key) {
		if (complexTypeSet.contains(key)) {
			log.error("Encountered cycle for key: {}", key);
			return true;
		}
		complexTypeSet.add(key);
		return false;
	}

	/**
	 * Returns a cached {@link JavaComplexType} if present, and upgrades its direction
	 * from ARGUMENT to BOTH if needed.
	 */
	private JavaComplexType getComplexTypeFromCache(ComplexTypeKey key, AttributeDirection direction) {
		JavaComplexType javaComplexType = complexTypeMap.get(key);
		if (javaComplexType != null) {
			if (direction == AttributeDirection.RETURN && javaComplexType.getAttributeDirection() == AttributeDirection.ARGUMENT) {
				changeAttributeDirection(javaComplexType);
			}
		}
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
	 * Populates the fields of a {@link JavaComplexType} given a {@link DBJavaClass},
	 * building nested types if necessary.
	 */
	private void populateComplexTypeFields(DBJavaClass dbJavaClass, AttributeDirection attributeDirection, JavaComplexType javaComplexType, SqlComplexType sqlComplexType) {
		List<DBJavaField> dbJavaFields = dbJavaClass.getFields();
		for (DBJavaField dbJavaField : dbJavaFields) {
			final JavaComplexType.Field field = buildJavaComplexField(dbJavaField, dbJavaClass);
			javaComplexType.addField(field);

			// If it's a primitive or directly supported type, add to the SQL type
			final SqlType sqlType = supportedTypes.get(field.getType());
			if (sqlType != null && dbJavaField.getArrayDepth() <= 0) {
				sqlComplexType.addField(field.getName(), sqlType.getSqlTypeName(), field.getFieldIndex());
			} else {
				// It's a nested complex field
				handleNestedField(field, dbJavaField, attributeDirection, sqlComplexType);
			}
		}
	}

	/**
	 * Builds a single {@link JavaComplexType.Field} instance from a {@link DBJavaField}.
	 */
	private JavaComplexType.Field buildJavaComplexField(DBJavaField dbJavaField, DBJavaClass dbJavaClass) {
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

		if (unsupportedTypes.contains(fieldParameter)) {
			log.error("Encountered unsupported type for field {}: {}", dbJavaField, fieldParameter);
		}

		// Basic field setup
		field.setFieldIndex(dbJavaField.getIndex());
		field.setName(dbJavaField.getName());
		field.setAccessModifier(dbJavaField.getAccessibility().toString());
		field.setType(fieldParameter, supportedTypes.get(fieldParameter));

		// If array
		if (dbJavaField.getArrayDepth() > 0) {
			field.setArray(true);
			field.setArrayDepth(dbJavaField.getArrayDepth());
		}

		// If the field is non-public, set up the getter/setter if present
		if (field.getAccessModifier() != JavaComplexType.Field.AccessModifier.PUBLIC) {
			field.setGetter(getGetter(field.getName(), fieldParameter, field.getArrayDepth(), dbJavaClass));
			field.setSetter(getSetter(field.getName(), fieldParameter, field.getArrayDepth(), dbJavaClass));
		}

		// If the underlying Java class is known
		if (dbJavaField.getFieldClass() != null) {
			field.setType(getNewSqlTypeName(dbJavaField.getFieldClass().getName()));
		}

		return field;
	}

	/**
	 * Handles a nested field that either references a nested array type or a nested complex type.
	 */
	private void handleNestedField(JavaComplexType.Field field, DBJavaField dbJavaField, AttributeDirection attributeDirection, SqlComplexType sqlComplexType) {
		field.setComplexType(true);
		JavaComplexType fieldJavaComplexType;
		if (dbJavaField.getArrayDepth() > 0) {
			// Nested array
			fieldJavaComplexType = createJavaComplexArrayType(dbJavaField.getFieldClass(), field.getType(), dbJavaField.getArrayDepth(), attributeDirection);
		} else {
			// Nested object
			fieldJavaComplexType = createJavaComplexType(dbJavaField.getFieldClass(), field.getType(), attributeDirection);
		}
		if (fieldJavaComplexType != null) {
			sqlComplexType.addField(field.getName(), fieldJavaComplexType.getCorrespondingSqlType().getName(), field.getFieldIndex());
		}
	}

	/**
	 * Finalizes a newly constructed {@link JavaComplexType}, storing it in the cache
	 * and linking its corresponding SQL type.
	 */
	private void finalizeComplexType(ComplexTypeKey key, JavaComplexType javaComplexType, SqlComplexType sqlComplexType) {
		sqlComplexType.setName(getNewSqlTypeName(javaComplexType.getTypeName()));
		javaComplexType.setCorrespondingSqlType(sqlComplexType);
		wrapper.addArgumentJavaComplexType(javaComplexType);

		complexTypeMap.put(key, javaComplexType);
		complexTypeSet.remove(key);
	}

	/**
	 * If a {@link JavaComplexType} was first encountered as an ARGUMENT but is also needed
	 * for RETURN, we mark it (and nested fields) as BOTH.
	 *
	 * @param javaComplexType The type to promote to BOTH directions.
	 */
	private void changeAttributeDirection(final JavaComplexType javaComplexType) {
		javaComplexType.setAttributeDirection(AttributeDirection.BOTH);

		if (javaComplexType.isArray()) {
			// For arrays, mark all corresponding array dimension entries + the base
			for (short i = 1; i <= javaComplexType.getArrayDepth(); i++) {
				final ComplexTypeKey complexTypeKey = new ComplexTypeKey(javaComplexType.getTypeName(), javaComplexType.getArrayDepth());
				final JavaComplexType mappedType = complexTypeMap.get(complexTypeKey);
				if (mappedType != null) {
					mappedType.setAttributeDirection(AttributeDirection.BOTH);
				}
			}
			// Also update the non-array variant if it exists
			final ComplexTypeKey baseKey = new ComplexTypeKey(javaComplexType.getTypeName(), (short) 0);
			JavaComplexType baseType = complexTypeMap.get(baseKey);
			if (baseType != null) {
				changeAttributeDirection(baseType);
			}
		} else {
			// For complex objects, recursively mark subfields
			for (JavaComplexType.Field field : javaComplexType.getFields()) {
				if (field.isComplexType()) {
					final ComplexTypeKey complexTypeKey = new ComplexTypeKey(field.getType(), field.getArrayDepth());
					JavaComplexType nested = complexTypeMap.get(complexTypeKey);
					if (nested != null) {
						changeAttributeDirection(nested);
					}
				}
			}
		}
	}

	/**
	 * Finds a setter method matching the given field signature, if one exists.
	 */
	private String getSetter(final String fieldName, final String fieldParameter, final short arrayDepth, final DBJavaClass dbJavaClass) {
		if (dbJavaClass == null) return null;

		final String setterName = "set" + capitalize(fieldName);
		List<DBJavaMethod> methods = dbJavaClass.getMethods();
		DBJavaMethod methodSet = null;
		for(DBJavaMethod method : methods){
			if(setterName.equals(method.getName().split("#")[0])){
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
					if(dbJavaClass.getField(fieldName) != null && dbJavaClass.getField(fieldName).getFieldClass() != null){
						targetFieldClass = convertClassNameToDotNotation(dbJavaClass.getField(fieldName).getFieldClass().getQualifiedName());
					} else {
						targetFieldClass = fieldParameter;
					}
					if (getParameterType(param).equals(targetFieldClass) && param.getArrayDepth() == arrayDepth) {
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
	private String getGetter(final String fieldName, final String fieldParameter, final short arrayDepth, final DBJavaClass dbJavaClass) {
		if (dbJavaClass == null) return null;

		final String getterName = "get" + capitalize(fieldName);
		List<DBJavaMethod> methods = dbJavaClass.getMethods();
		DBJavaMethod methodGet = null;
		for(DBJavaMethod method : methods){
			if(getterName.equals(method.getName().split("#")[0])){
				methodGet = method;
				break;
			}
		}

		if (methodGet != null) {
			try {
				final String methodReturn = getParameterType(methodGet.getReturnType(), methodGet.getClassName());
				if (methodReturn.equals(fieldParameter) && methodGet.getArrayDepth() == arrayDepth && methodGet.getParameters().isEmpty()) {
					return methodGet.getName().split("#")[0];
				}
			} catch (Exception e) {
				log.error("Could not get Getter method for Field {} in class {}", fieldName, dbJavaClass, e);
				conditionallyLog(e);
			}
		}
		return null;
	}

	/**
	 * Retrieves the parameter type from a {@link DBJavaParameter}, substituting if the
	 * type is empty or a placeholder.
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
	 * Builds a new SQL type name, prepending {@link #newSqlTypePrepend}.
	 */
	private String getNewSqlTypeName(final String className) {
		if(!complexTypeConversion.containsKey(className)){
			complexTypeConversion.put(className, newSQLTypeCounter++);
		}
		return newSqlTypePrepend + complexTypeConversion.get(className);
	}


	/**
	 * Retrieves the package name from a slash-delimited path by converting it to dot notation,
	 * then taking everything before the last '.'.
	 */
	public static String getPackageName(final String fullyQualifiedClassName) {
		final String dotSeparatedString = convertClassNameToDotNotation(fullyQualifiedClassName);
		if (dotSeparatedString.lastIndexOf('.') != -1) {
			return dotSeparatedString.substring(0, dotSeparatedString.lastIndexOf('.'));
		} else {
			return "";
		}
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

	/**
	 * A composite key of {@code className + arrayLength} used to ensure uniqueness
	 * when creating/looking up complex types. This prevents collisions for arrays
	 * of the same class at different depths.
	 */
	@Getter
	public static class ComplexTypeKey {
		private final String className;
		private final short arrayLength;

		public ComplexTypeKey(final String className, final short arrayLength) {
			this.className = className;
			this.arrayLength = arrayLength;
		}
	}
}