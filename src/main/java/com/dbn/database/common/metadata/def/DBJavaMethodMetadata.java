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

package com.dbn.database.common.metadata.def;

import com.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBJavaMethodMetadata extends DBObjectMetadata {
	String getClassName() throws SQLException;

	String getReturnType() throws SQLException;

	String getReturnClassName() throws SQLException;

	String getMethodName() throws SQLException;

	String getMethodSignature() throws SQLException;

	String getAccessibility() throws SQLException;

	short getMethodIndex() throws SQLException;

	short getArrayDepth() throws SQLException;

	boolean isStatic() throws SQLException;

	boolean isFinal() throws SQLException;

	boolean isAbstract() throws SQLException;
}
