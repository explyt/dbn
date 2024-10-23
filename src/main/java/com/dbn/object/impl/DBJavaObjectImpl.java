package com.dbn.object.impl;

import com.dbn.common.icon.CompositeIcon;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaObjectMetadata;
import com.dbn.object.DBJavaObject;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBSchemaObjectImpl;
import com.dbn.object.common.list.DBObjectListContainer;
import com.dbn.object.type.DBObjectType;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.SQLException;

import static com.dbn.object.type.DBObjectType.*;

public class DBJavaObjectImpl extends DBSchemaObjectImpl<DBJavaObjectMetadata> implements DBJavaObject {

	String kind;
	String accessibility;
	boolean isAbstract;
	boolean isFinal;
	boolean isStatic;
	boolean isInner;

	DBJavaObjectImpl(DBSchema schema, DBJavaObjectMetadata metadata) throws SQLException {
		super(schema, metadata);
	}

	@Override
	public @NotNull DBObjectType getObjectType() {
		return DBObjectType.JAVA_OBJECT;
	}

	@Override
	protected String initObject(ConnectionHandler connection, DBObject parentObject, DBJavaObjectMetadata metadata) throws SQLException {
		this.kind = metadata.getKind();
		this.accessibility = metadata.getAccessibility();
		this.isFinal = metadata.isFinal();
		this.isAbstract = metadata.isAbstract();
		this.isStatic = metadata.isStatic();
		this.isInner = metadata.isInner();
		return metadata.getName().replace("/",".");
	}


	@Override
	protected void initLists(ConnectionHandler connection) {
		super.initLists(connection);
		DBSchema schema = getSchema();
		DBObjectListContainer childObjects = ensureChildObjects();

		childObjects.createSubcontentObjectList(JAVA_OBJECT, this, schema);
	}

	@Override
	@Nullable
	public Icon getIcon() {
		Icon baseIcon;

		if(this.kind.equals("ENUM"))
			baseIcon = AllIcons.Nodes.Enum;
		else if(this.kind.equals("INTERFACE"))
			baseIcon = AllIcons.Nodes.Interface;
		else if(this.isAbstract)
			baseIcon = AllIcons.Nodes.AbstractClass;
		else
			baseIcon = AllIcons.Nodes.Class;

		if(this.isFinal)
			baseIcon = new CompositeIcon(baseIcon,AllIcons.Nodes.FinalMark,-17);

		if(this.accessibility != null && this.accessibility.equals("PRIVATE"))
			baseIcon = new CompositeIcon(baseIcon, AllIcons.Nodes.Private,-10);

		return baseIcon ;
	}

	@Override
	public boolean isFinal() {
		return this.isFinal;
	}

	@Override
	public boolean isAbstract() {
		return this.isAbstract;
	}

	@Override
	public boolean isStatic() {
		return this.isStatic;
	}

	@Override
	public boolean isInner() {
		return this.isInner;
	}

	@Override
	public String getKind() {
		return this.kind;
	}

	@Override
	public String getAccessibility() {
		return this.accessibility;
	}
}
