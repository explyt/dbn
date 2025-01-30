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

import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaParameterMetadata;
import com.dbn.object.DBJavaClass;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBJavaParameter;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectImpl;
import com.dbn.object.lookup.DBJavaClassRef;
import com.dbn.object.type.DBJavaValueType;
import com.dbn.object.type.DBObjectType;
import com.intellij.core.CoreJavaCodeStyleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.psi.codeStyle.VariableKind;
import lombok.Getter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import static com.dbn.object.common.property.DBObjectProperty.CLASS;

@Getter
public class DBJavaParameterImpl extends DBObjectImpl<DBJavaParameterMetadata> implements DBJavaParameter {
	private short methodIndex;
	private short position;
	private short arrayDepth;

	private String baseType;
	private DBJavaClassRef parameterClass;

	public DBJavaParameterImpl(@NotNull DBJavaMethod javaMethod, DBJavaParameterMetadata metadata) throws SQLException {
		super(javaMethod, metadata);
	}

	@Override
	public @NotNull DBObjectType getObjectType() {
		return DBObjectType.JAVA_PARAMETER;
	}

	@Override
	protected void initLists(ConnectionHandler connection) {
		super.initLists(connection);
	}

	@NonNls
	@Override
	protected String initObject(ConnectionHandler connection, DBObject parentObject, DBJavaParameterMetadata metadata) throws SQLException {
		methodIndex = metadata.getMethodIndex();
		position = metadata.getArgumentPosition();
		arrayDepth = metadata.getArrayDepth();
		baseType = metadata.getBaseType();

		if (baseType.equals("class")) set(CLASS, true);

		String argumentClass = metadata.getArgumentClass();

		String arrMatrix = arrayDepth > 0 ? "[]".repeat(arrayDepth) : "";

		if (!isPrimitive()) {
			parameterClass = new DBJavaClassRef(parentObject.getSchema(), argumentClass, "SYS");
			//String className = argumentClass.substring(argumentClass.lastIndexOf("/") + 1);
			//return className + arrMatrix + " p" + position;
		} else {
			//return parameterType + arrMatrix + " p" + position;
		}
		//return createParameterName(parentObject.getProject(), isClass ? argumentClass : parameterType);
		return "param" + position;
	}

	private String createParameterName(Project project, String parameterType) {
		String[] tokens = parameterType.split("[./]");
		String lastToken = tokens[tokens.length - 1];

		JavaCodeStyleManager codeStyleManager = CoreJavaCodeStyleManager.getInstance(project);
		SuggestedNameInfo suggestedNames = codeStyleManager.suggestVariableName(VariableKind.PARAMETER, lastToken, null, null);
		if (suggestedNames.names.length > 0) return suggestedNames.names[0] + position;

		return tokens[tokens.length - 1] + position;
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
	public boolean isPlainValueType() {
		return isPrimitive() || getValueType() != null;
	}

	@Nullable
	@Override
	public DBJavaValueType getValueType() {
		return isClass() ?
				DBJavaValueType.forPath(parameterClass.getClassName()):
				DBJavaValueType.forName(baseType);
	}

	@Override
	public DBJavaClass getParameterClass() {
		return parameterClass == null ? null : parameterClass.get();
	}

	@Override
	public String getPresentableText() {
		return super.getPresentableText();
	}

	@Override
	public String getParameterTypeName() {
		return parameterClass == null ? baseType : parameterClass.getClassSimpleName();
	}

	@Override
	public short getPosition() {
		return position;
	}
}
