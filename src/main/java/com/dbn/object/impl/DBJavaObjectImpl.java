/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.object.impl;

import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaObjectMetadata;
import com.dbn.database.interfaces.DatabaseDataDefinitionInterface;
import com.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dbn.editor.DBContentType;
import com.dbn.object.DBJavaObject;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBSchemaObjectImpl;
import com.dbn.object.type.DBJavaObjectAccessibility;
import com.dbn.object.type.DBJavaObjectKind;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.SQLException;

import static com.dbn.common.Priority.HIGHEST;
import static com.dbn.object.common.property.DBObjectProperty.ABSTRACT;
import static com.dbn.object.common.property.DBObjectProperty.BINARY_AVAILABLE;
import static com.dbn.object.common.property.DBObjectProperty.COMPILABLE;
import static com.dbn.object.common.property.DBObjectProperty.DEBUGABLE;
import static com.dbn.object.common.property.DBObjectProperty.FINAL;
import static com.dbn.object.common.property.DBObjectProperty.INNER;
import static com.dbn.object.common.property.DBObjectProperty.INVALIDABLE;
import static com.dbn.object.common.property.DBObjectProperty.SOURCE_AVAILABLE;
import static com.dbn.object.common.property.DBObjectProperty.STATIC;
import static com.dbn.object.type.DBJavaObjectKind.ENUM;
import static com.dbn.object.type.DBJavaObjectKind.INTERFACE;

@Getter
public class DBJavaObjectImpl extends DBSchemaObjectImpl<DBJavaObjectMetadata> implements DBJavaObject {

	private DBJavaObjectKind kind;
	private DBJavaObjectAccessibility accessibility;

	DBJavaObjectImpl(DBSchema schema, DBJavaObjectMetadata metadata) throws SQLException {
		super(schema, metadata);
	}

	@Override
	public @NotNull DBObjectType getObjectType() {
		return DBObjectType.JAVA_OBJECT;
	}

	@Override
	protected String initObject(ConnectionHandler connection, DBObject parentObject, DBJavaObjectMetadata metadata) throws SQLException {
		this.kind = DBJavaObjectKind.get(metadata.getObjectKind());
		this.accessibility = DBJavaObjectAccessibility.get(metadata.getObjectAccessibility());

		set(FINAL, metadata.isFinal());
		set(ABSTRACT, metadata.isAbstract());
		set(STATIC, metadata.isStatic());
		set(INNER, metadata.isInner());
		set(SOURCE_AVAILABLE, metadata.isSourceAvailable());
		set(BINARY_AVAILABLE, metadata.isBinaryAvailable());

		return metadata.getObjectName().replace("/",".");
	}


	@Override
	protected void initLists(ConnectionHandler connection) {
		super.initLists(connection);
/*
		// TODO support inner classes as child objects
		DBSchema schema = getSchema();
		DBObjectListContainer childObjects = ensureChildObjects();

		childObjects.createSubcontentObjectList(JAVA_OBJECT, this, schema);
*/
	}

	public void initProperties() {
		super.initProperties();
		properties.set(COMPILABLE, true);
		properties.set(INVALIDABLE, true);
		properties.set(DEBUGABLE, true);
	}

	@Override
	@Nullable
	public Icon getIcon() {
		if (kind == ENUM) return Icons.DBO_JAVA_ENUMERATION;
		if (kind == INTERFACE) return Icons.DBO_JAVA_ENUMERATION;
		if (isAbstract()) return Icons.DBO_JAVA_CLASS_ABSTRACT;
		return Icons.DBO_JAVA_CLASS;

		// TODO "final" - new CompositeIcon(baseIcon, AllIcons.Nodes.Private, -10);
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
	public boolean isSourceAvailable() {
		return is(SOURCE_AVAILABLE);
	}

	@Override
	public boolean isBinaryAvailable() {
		return is(BINARY_AVAILABLE);
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
					DatabaseDataDefinitionInterface dataDefinition = connection.getDataDefinitionInterface();
					dataDefinition.updateJavaObject(getName(), newCode, conn);
				});
	}
}
