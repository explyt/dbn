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

import com.dbn.browser.model.BrowserTreeNode;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaMethodMetadata;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBJavaObject;
import com.dbn.object.DBJavaParameter;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectImpl;
import com.dbn.object.common.list.DBObjectList;
import com.dbn.object.common.list.DBObjectListContainer;
import com.dbn.object.common.list.DBObjectNavigationList;
import com.dbn.object.lookup.DBJavaObjectRef;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dbn.object.common.property.DBObjectProperty.PUBLIC;
import static com.dbn.object.common.property.DBObjectProperty.STATIC;

@Getter
public class DBJavaMethodImpl extends DBObjectImpl<DBJavaMethodMetadata> implements DBJavaMethod {
	private short index;
	private short overload;
	private String className;
	private String returnType;
	private DBJavaObjectRef returnClass;

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
		returnType = metadata.getReturnType();

		String returnClassName = metadata.getReturnClassName();
		if (returnClassName != null) {
			DBSchema schema = parentObject.getSchema();
			returnClass = new DBJavaObjectRef(schema, returnClassName, "SYS");
		}

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
	public String getPresentableText() {
		DBObjectList<DBJavaParameter> parameterList = getChildObjectList(DBObjectType.JAVA_PARAMETER);
		StringBuilder builder = new StringBuilder();
		builder.append(getName());
		builder.append("(");

		if (parameterList != null) {
			if (parameterList.isLoaded()) {
				String parameters = parameterList
						.getElements()
						.stream()
						.map(p -> p.getParameterTypeName())
						.collect(Collectors.joining(", "));
				builder.append(parameters);
			} else if (!parameterList.isLoading()){
				parameterList.loadInBackground();
			}
		}
		builder.append("): ");

		if (returnClass == null) {
			builder.append(returnType);
		} else {
			builder.append(returnClass.getClassSimpleName());
		}
		return builder.toString();
	}

	public DBJavaObject getReturnClass() {
		return returnClass == null ? null : returnClass.get();
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

	@Override
	protected @Nullable List<DBObjectNavigationList> createNavigationLists() {
		List<DBObjectNavigationList> navigationLists = new LinkedList<>();
		navigationLists.add(DBObjectNavigationList.create("Parameters", getParameters()));
		navigationLists.add(DBObjectNavigationList.create("Return Type", getReturnClass()));
		return navigationLists;
	}

	/*********************************************************
	 *                     TreeElement                       *
	 *********************************************************/
	@Override
	@NotNull
	public List<BrowserTreeNode> buildPossibleTreeChildren() {
		return super.buildPossibleTreeChildren();
/*
		return DatabaseBrowserUtils.createList(
				getChildObjectList(DBObjectType.JAVA_PARAMETER));
*/
	}

	@Override
	public boolean hasVisibleTreeChildren() {
		return false;
/*
		ObjectTypeFilterSettings settings = getObjectTypeFilterSettings();
		return settings.isVisible(DBObjectType.JAVA_PARAMETER);
*/
	}
}
