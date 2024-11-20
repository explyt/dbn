
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

package com.dbn.database.common.metadata.impl;

import com.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dbn.database.common.metadata.def.DBJavaParameterMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBJavaParameterMetadataImpl extends DBObjectMetadataBase implements DBJavaParameterMetadata {

	public DBJavaParameterMetadataImpl(ResultSet resultSet) {
		super(resultSet);
	}

	@Override
	public String getClassName() throws SQLException {
		return getString("CLASS_NAME");
	}

	@Override
	public String getMethodName() throws SQLException {
		return getString("METHOD_NAME");
	}

	@Override
	public short getMethodIndex() throws SQLException {
		return resultSet.getShort("METHOD_INDEX");
	}

	@Override
	public short getMethodOverload() throws SQLException {
		return resultSet.getShort("METHOD_OVERLOAD");
	}

	@Override
	public short getArgumentPosition() throws SQLException {
		return resultSet.getShort("ARGUMENT_POSITION");
	}

	@Override
	public short getArrayDepth() throws SQLException {
		return resultSet.getShort("ARRAY_DEPTH");
	}

	@Override
	public String getBaseType() throws SQLException {
		return getString("BASE_TYPE");
	}

	@Override
	public String getArgumentClass() throws SQLException {
		return getString("ARGUMENT_CLASS");
	}
}
