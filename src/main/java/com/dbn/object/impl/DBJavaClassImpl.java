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
import com.dbn.common.icon.Icons;
import com.dbn.common.util.Strings;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaClassMetadata;
import com.dbn.database.interfaces.DatabaseDataDefinitionInterface;
import com.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dbn.editor.DBContentType;
import com.dbn.object.DBJavaClass;
import com.dbn.object.DBJavaField;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBSchemaObjectImpl;
import com.dbn.object.common.list.DBObjectListContainer;
import com.dbn.object.common.status.DBObjectStatus;
import com.dbn.object.common.status.DBObjectStatusHolder;
import com.dbn.object.filter.type.ObjectTypeFilterSettings;
import com.dbn.object.lookup.DBJavaNameCache;
import com.dbn.object.lookup.DBObjectRef;
import com.dbn.object.type.DBJavaAccessibility;
import com.dbn.object.type.DBJavaClassKind;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.List;

import static com.dbn.common.Priority.HIGHEST;
import static com.dbn.common.util.Lists.filter;
import static com.dbn.object.common.property.DBObjectProperty.ABSTRACT;
import static com.dbn.object.common.property.DBObjectProperty.COMPILABLE;
import static com.dbn.object.common.property.DBObjectProperty.DEBUGABLE;
import static com.dbn.object.common.property.DBObjectProperty.EDITABLE;
import static com.dbn.object.common.property.DBObjectProperty.FINAL;
import static com.dbn.object.common.property.DBObjectProperty.INNER;
import static com.dbn.object.common.property.DBObjectProperty.INVALIDABLE;
import static com.dbn.object.common.property.DBObjectProperty.PRIMITIVE;
import static com.dbn.object.common.property.DBObjectProperty.STATIC;
import static com.dbn.object.type.DBJavaClassKind.ENUM;
import static com.dbn.object.type.DBJavaClassKind.INTERFACE;
import static com.dbn.object.type.DBObjectType.JAVA_CLASS;
import static com.dbn.object.type.DBObjectType.JAVA_FIELD;
import static com.dbn.object.type.DBObjectType.JAVA_INNER_CLASS;
import static com.dbn.object.type.DBObjectType.JAVA_METHOD;

@Getter
public class DBJavaClassImpl extends DBSchemaObjectImpl<DBJavaClassMetadata> implements DBJavaClass {
	private DBObjectRef<DBJavaClass> outerClass;

	private DBJavaClassKind kind;
	private DBJavaAccessibility accessibility;

	public DBJavaClassImpl(DBSchema schema, DBJavaClassMetadata metadata) throws SQLException {
		super(schema, metadata);

		String outerClassName = metadata.getOuterClassName();
		if (Strings.isNotEmpty(outerClassName)) {
			outerClass = new DBObjectRef<>(schema.ref(), DBObjectType.JAVA_CLASS, outerClassName);
			ref.clearReference();
			ref.setParent(outerClass);
		}
	}

	@Override
	public @NotNull DBObjectType getObjectType() {
		return JAVA_CLASS;
	}

	@Override
	protected String initObject(ConnectionHandler connection, DBObject parentObject, DBJavaClassMetadata metadata) throws SQLException {
		this.kind = DBJavaClassKind.get(metadata.getObjectKind());
		this.accessibility = DBJavaAccessibility.get(metadata.getAccessibility());

		set(FINAL, metadata.isFinal());
		set(ABSTRACT, metadata.isAbstract());
		set(STATIC, metadata.isStatic());
		set(INNER, metadata.isInner());
		set(PRIMITIVE, metadata.isPrimitive());

		return metadata.getObjectName();
	}


	@Override
	protected void initLists(ConnectionHandler connection) {
		super.initLists(connection);

		// TODO support inner classes as child objects
		DBSchema schema = getSchema();
		DBObjectListContainer childObjects = ensureChildObjects();
		childObjects.createSubcontentObjectList(JAVA_INNER_CLASS, this, schema);
		childObjects.createSubcontentObjectList(JAVA_FIELD, this, schema);
		childObjects.createSubcontentObjectList(JAVA_METHOD, this, schema);
	}

	public void initProperties() {
		super.initProperties();
		properties.set(COMPILABLE, true);
		properties.set(INVALIDABLE, true);
		properties.set(DEBUGABLE, true);
		properties.set(EDITABLE, !isInner());
	}

	public void initStatus(DBJavaClassMetadata metadata) throws SQLException {
		boolean isValid = metadata.isValid();
		boolean isDebug = metadata.isDebug();
		DBObjectStatusHolder objectStatus = getStatus();
		objectStatus.set(DBObjectStatus.VALID, isValid);
		objectStatus.set(DBObjectStatus.DEBUG, isDebug);
		objectStatus.set(DBContentType.CODE, DBObjectStatus.PRESENT, true);
	}

	@Override
	public String getPresentableText() {
		return isInner() ?
                getSimpleName() :
                getCanonicalName();

	}

	@Override
	public String getCanonicalName() {
		return DBJavaNameCache.getCanonicalName(getName());
	}

	@Override
	public String getSimpleName() {
		return DBJavaNameCache.getSimpleName(getName());
	}

	@Override
	@Nullable
	public Icon getIcon() {
		if (kind == ENUM) return withErrorMarker(Icons.DBO_JAVA_ENUMERATION);
		if (kind == INTERFACE) return withErrorMarker(Icons.DBO_JAVA_INTERFACE);
		if (isAbstract()) return withErrorMarker(Icons.DBO_JAVA_CLASS_ABSTRACT);
		return withErrorMarker(withFinalMarker(Icons.DBO_JAVA_CLASS));
	}

	private Icon withErrorMarker(Icon base) {
		return isInvalid() ? Icons.withErrorMarker(base) : base;
	}

	private Icon withFinalMarker(Icon base) {
		return isFinal() ? Icons.withPinMarker(base): base;
	}

	private boolean isInvalid() {
		return getObjectStatus().isNot(DBObjectStatus.VALID);
	}

	@Override
	public boolean isFinal() {
		return is(FINAL);
	}

	@Override
	public boolean isAbstract() {
		return is(ABSTRACT);
	}

	@Override
	public boolean isStatic() {
		return is(STATIC);
	}

	@Override
	public boolean isInner() {
		return is(INNER);
	}

	@Override
	public boolean isPrimitive() {
		return is(PRIMITIVE);
	}

	@Override
	public List<DBJavaMethod> getMethods() {
		return getChildObjects(JAVA_METHOD);
	}

	@Override
	public List<DBJavaMethod> getStaticMethods() {
		return filter(getMethods(), m -> m.isStatic());
	}

	@Override
	public DBJavaMethod getMethod(String name) {
		return getChildObject(JAVA_METHOD, name);
	}

	@Override
	public List<DBJavaField> getFields() {
		return getChildObjects(JAVA_FIELD);
	}

	@Override
	public DBJavaField getField(String name) {
		return getChildObject(JAVA_FIELD, name);
	}

	@Override
	public List<DBJavaClass> getInnerClasses(){
		return getChildObjects(JAVA_INNER_CLASS);
	}

	@Override
	public DBJavaClass getInnerClass(String name){
		return getChildObject(JAVA_INNER_CLASS, name);
	}

	@Nullable
	public DBJavaClass getOuterClass() {
		return isInner() ? DBObjectRef.get(outerClass) : null;
	}

	/*********************************************************
	 *                  DBEditableCodeObject                 *
	 ********************************************************/

	@Override
	public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {

		DatabaseInterfaceInvoker.execute(HIGHEST,
				"Updating source code",
				"Updating sources of " + getQualifiedNameWithType(),
				getProject(),
				getConnectionId(),
				getSchemaId(),
				conn -> {
					ConnectionHandler connection = getConnection();
					DatabaseDataDefinitionInterface dataDefinitionInterface = connection.getDataDefinitionInterface();
					dataDefinitionInterface.updateJavaClass(getName(true), newCode, conn);

					DatabaseMetadataInterface metadataInterface = connection.getMetadataInterface();
					metadataInterface.compileJavaClass(getSchemaName(true), getName(true), conn);
				});
	}

	/*********************************************************
	 *                     TreeElement                       *
	 *********************************************************/
	@Override
	@NotNull
	public List<BrowserTreeNode> buildPossibleTreeChildren() {
		return DatabaseBrowserUtils.createList(
				getChildObjectList(JAVA_FIELD),
				getChildObjectList(JAVA_METHOD),
				getChildObjectList(JAVA_INNER_CLASS));
	}

	@Override
	public boolean hasVisibleTreeChildren() {
		ObjectTypeFilterSettings settings = getObjectTypeFilterSettings();
		return settings.isVisible(JAVA_FIELD) ||
				settings.isVisible(JAVA_METHOD) ||
				settings.isVisible(JAVA_INNER_CLASS);
	}
}
