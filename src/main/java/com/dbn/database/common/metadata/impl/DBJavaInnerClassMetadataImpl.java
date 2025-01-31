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

package com.dbn.database.common.metadata.impl;

import com.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dbn.database.common.metadata.def.DBJavaInnerClassMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBJavaInnerClassMetadataImpl extends DBObjectMetadataBase implements DBJavaInnerClassMetadata {

	public DBJavaInnerClassMetadataImpl(ResultSet resultSet) {
		super(resultSet);
	}

	@Override
	public String getClassName() throws SQLException {
		return getString("CLASS_NAME");
	}

	@Override
	public String getObjectName() throws SQLException {
		return getString("OBJECT_NAME");
	}

	@Override
	public String getObjectKind() throws SQLException {
		return getString("OBJECT_KIND");
	}

	@Override
	public String getAccessibility() throws SQLException {
		return getString("ACCESSIBILITY");
	}

	@Override
	public boolean isFinal() throws SQLException {
		return isYesFlag("IS_FINAL");
	}

	@Override
	public boolean isAbstract() throws SQLException {
		return isYesFlag("IS_ABSTRACT");
	}

	@Override
	public boolean isStatic() throws SQLException {
		return isYesFlag("IS_STATIC");
	}

	@Override
	public boolean isValid() throws SQLException {
		return isYesFlag("IS_VALID");
	}

	@Override
	public boolean isDebug() throws SQLException {
		return isYesFlag("IS_DEBUG");
	}
}
