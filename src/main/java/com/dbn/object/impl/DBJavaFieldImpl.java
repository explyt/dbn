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

package com.dbn.object.impl;

import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaFieldMetadata;
import com.dbn.object.DBJavaClass;
import com.dbn.object.DBJavaField;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectImpl;
import com.dbn.object.lookup.DBJavaClassRef;
import com.dbn.object.type.DBJavaAccessibility;
import com.dbn.object.type.DBJavaValueType;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.Objects;

import static com.dbn.object.common.property.DBObjectProperty.CLASS;
import static com.dbn.object.common.property.DBObjectProperty.FINAL;
import static com.dbn.object.common.property.DBObjectProperty.STATIC;

@Getter
public class DBJavaFieldImpl extends DBObjectImpl<DBJavaFieldMetadata> implements DBJavaField {
	private short index;
	private short arrayDepth;
	private String baseType;
	private String className;
	private DBJavaClassRef javaClass;
	private DBJavaAccessibility accessibility;

	public DBJavaFieldImpl(@NotNull DBJavaClass javaClass, DBJavaFieldMetadata metadata) throws SQLException {
		super(javaClass, metadata);
	}

	@Override
	public @NotNull DBObjectType getObjectType() {
		return DBObjectType.JAVA_FIELD;
	}

	@Override
	protected String initObject(ConnectionHandler connection, DBObject parentObject, DBJavaFieldMetadata metadata) throws SQLException {
		index = metadata.getFieldIndex();
		arrayDepth = metadata.getArrayDepth();
		baseType = metadata.getBaseType();
		className = metadata.getClassName();

		if (Objects.equals(baseType, "class")) set(CLASS, true);

		if(metadata.getAccessibility() == null){
			accessibility = DBJavaAccessibility.PACKAGE_PRIVATE;
		} else {
			accessibility = DBJavaAccessibility.get(metadata.getAccessibility());
		}
		String fieldClassName = metadata.getFieldClass();
		if (fieldClassName != null) {
			DBSchema schema = parentObject.getSchema();
			javaClass = new DBJavaClassRef(schema, fieldClassName, "SYS");
		}

		set(STATIC, metadata.isStatic());
		set(FINAL, metadata.isFinal());
		return metadata.getFieldName();
	}

	@Override
	public short getPosition() {
		return index;
	}

	@Override
	@Nullable
	public Icon getIcon() {
		// TODO accessibility overlays
		return Icons.DBO_JAVA_FIELD;
	}

	@Override
	public DBJavaClass getJavaClass() {
		return javaClass == null ? null : javaClass.get();
	}

	public String getJavaClassName() {
		return javaClass == null ? null : javaClass.getObjectName();
	}

	@Override
	public DBJavaClass getOwnerClass() {
		return getParentObject();
	}

	@Override
	public String getOwnerClassName() {
		return getOwnerClass().getName();
	}

	@Override
	public boolean isStatic() {
		return is(STATIC);
	}

	@Override
	public boolean isFinal() {
		return is(FINAL);
	}

	@Override
	public boolean isArray() {
		return arrayDepth > 0;
	}

	@Override
	public boolean isClass() {
		return is(CLASS);
	}

	@Override
	public boolean isPrimitive() {
		return !isClass();
	}

	@Override
	public boolean isPlainValue() {
		return isPrimitive() || getValueType() != null;
	}

	@Override
	public @Nullable DBJavaValueType getValueType() {
		return isClass() ?
				DBJavaValueType.forObjectName(javaClass.getObjectName()):
				DBJavaValueType.forName(baseType);
	}
}