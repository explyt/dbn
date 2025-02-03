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

import com.dbn.database.common.metadata.def.DBJavaClassMetadata;
import com.dbn.object.DBJavaClass;
import com.dbn.object.DBJavaInnerClass;
import com.dbn.object.common.property.DBObjectProperty;
import com.dbn.object.lookup.DBJavaClassRef;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@Getter
public class DBJavaInnerClassImpl extends DBJavaClassImpl implements DBJavaInnerClass {
	private final DBJavaClassRef outerClass;

	DBJavaInnerClassImpl(DBJavaClass outerClass, DBJavaClassMetadata metadata) throws SQLException {
		super(outerClass, metadata);
		this.outerClass = new DBJavaClassRef(outerClass.getSchema(), outerClass.getName());
	}

	public DBJavaClass getOuterClass() {
		return outerClass.get();
	}

	@Override
	public void initProperties() {
		// not directly editable (edit through owner class)
		properties.set(DBObjectProperty.EDITABLE, false);
	}

	@Override
	public @NotNull DBObjectType getObjectType() {
		return DBObjectType.JAVA_INNER_CLASS;
	}

}
