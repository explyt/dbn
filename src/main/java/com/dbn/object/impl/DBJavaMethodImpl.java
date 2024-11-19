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

import com.dbn.api.object.DBJavaClass;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaMethodMetadata;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBJavaObject;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectImpl;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dbn.object.common.property.DBObjectProperty.PUBLIC;
import static com.dbn.object.common.property.DBObjectProperty.STATIC;

@Getter
public class DBJavaMethodImpl extends DBObjectImpl<DBJavaMethodMetadata> implements DBJavaMethod {
	protected short overload;
	protected short position;

	public DBJavaMethodImpl(@NotNull DBJavaObject javaObject, DBJavaMethodMetadata metadata) throws SQLException {
		super(javaObject, metadata);
	}

	@Override
	public @NotNull DBObjectType getObjectType() {
		return DBObjectType.JAVA_METHOD;
	}

	@Override
	protected void initLists(ConnectionHandler connection) {
		super.initLists(connection);
	}

	@Override
	protected String initObject(ConnectionHandler connection, DBObject parentObject, DBJavaMethodMetadata metadata) throws SQLException {
		position = metadata.getPosition();
		overload = metadata.getOverload();

		set(PUBLIC,metadata.isPublic());
		set(STATIC,metadata.isStatic());
		return metadata.getMethodName();
	}

	@Override
	public String getPresentableTextDetails() {
		return overload > 0 ? " #" + overload : "";
	}

	@Override
	public DBJavaClass getJavaClass() {
		return getParentObject();
	}

	@Override
	public boolean isPublic() {
		return is(PUBLIC);
	}

	@Override
	public boolean isStatic() {
		return is(STATIC);
	}
}
