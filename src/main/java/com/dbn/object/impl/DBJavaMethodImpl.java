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

import com.dbn.browser.DatabaseBrowserUtils;
import com.dbn.browser.model.BrowserTreeNode;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaMethodMetadata;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBJavaObject;
import com.dbn.object.DBJavaParameter;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectImpl;
import com.dbn.object.common.list.DBObjectListContainer;
import com.dbn.object.filter.type.ObjectTypeFilterSettings;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

import static com.dbn.object.common.property.DBObjectProperty.PUBLIC;
import static com.dbn.object.common.property.DBObjectProperty.STATIC;

@Getter
public class DBJavaMethodImpl extends DBObjectImpl<DBJavaMethodMetadata> implements DBJavaMethod {
	protected short index;
	protected short overload;
	protected String className;

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

		DBSchema schema = getSchema();
		DBObjectListContainer childObjects = ensureChildObjects();
		childObjects.createSubcontentObjectList(DBObjectType.JAVA_PARAMETER, this, schema);

	}

	@Override
	protected String initObject(ConnectionHandler connection, DBObject parentObject, DBJavaMethodMetadata metadata) throws SQLException {
		index = metadata.getMethodIndex();
		overload = metadata.getOverload();
		className = metadata.getClassName();

		set(PUBLIC,metadata.isPublic());
		set(STATIC,metadata.isStatic());
		return metadata.getMethodName();
	}

	@Override
	public short getPosition() {
		return index;
	}

	@Override
	public String getPresentableTextDetails() {
		return overload > 0 ? " #" + overload : "";
	}

	@Override
	public List<DBJavaParameter> getParameters() {
		return getChildObjects(DBObjectType.JAVA_PARAMETER);
	}

	@Override
	public DBJavaObject getJavaObject() {
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

	/*********************************************************
	 *                     TreeElement                       *
	 *********************************************************/
	@Override
	@NotNull
	public List<BrowserTreeNode> buildPossibleTreeChildren() {
		return DatabaseBrowserUtils.createList(
				getChildObjectList(DBObjectType.JAVA_PARAMETER));
	}

	@Override
	public boolean hasVisibleTreeChildren() {
		ObjectTypeFilterSettings settings = getObjectTypeFilterSettings();
		return settings.isVisible(DBObjectType.JAVA_PARAMETER);
	}
}
