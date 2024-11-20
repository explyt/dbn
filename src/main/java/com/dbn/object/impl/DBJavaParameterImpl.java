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
 *
 */

package com.dbn.object.impl;

import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaParameterMetadata;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBJavaParameter;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectImpl;
import com.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Objects;

public class DBJavaParameterImpl extends DBObjectImpl<DBJavaParameterMetadata> implements DBJavaParameter {

	protected short methodIndex;
	protected short position;
	protected short arrayDimension;

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

	@Override
	protected String initObject(ConnectionHandler connection, DBObject parentObject, DBJavaParameterMetadata metadata) throws SQLException {
		methodIndex = metadata.getMethodIndex();
		position = metadata.getArgumentPosition();
		arrayDimension = metadata.getArrayDepth();

		String arrMatrix = arrayDimension > 0 ? " []".repeat(arrayDimension) : "";

		String baseType = metadata.getBaseType();
		if(Objects.equals(baseType, "class")){
			return metadata.getArgumentClass().replace("/", ".") + arrMatrix;
		}
		return baseType + arrMatrix;
	}

	@Override
	public short getPosition() {
		return position;
	}
}
