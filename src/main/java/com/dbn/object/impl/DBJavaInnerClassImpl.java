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

import com.dbn.common.ref.WeakRefCache;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBJavaInnerClassMetadata;
import com.dbn.object.DBJavaClass;
import com.dbn.object.DBJavaInnerClass;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectImpl;
import com.dbn.object.type.DBJavaAccessibility;
import com.dbn.object.type.DBJavaClassKind;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dbn.object.common.property.DBObjectProperty.ABSTRACT;
import static com.dbn.object.common.property.DBObjectProperty.FINAL;
import static com.dbn.object.common.property.DBObjectProperty.STATIC;

@Getter
public class DBJavaInnerClassImpl extends DBObjectImpl<DBJavaInnerClassMetadata> implements DBJavaInnerClass {
	private DBJavaClassKind kind;
	private DBJavaAccessibility accessibility;
	private static final WeakRefCache<DBJavaInnerClass, String> presentableNameCache = WeakRefCache.weakKey();

	DBJavaInnerClassImpl(DBJavaClass javaClass, DBJavaInnerClassMetadata metadata) throws SQLException {
		super(javaClass, metadata);
	}

	@Override
	public @NotNull DBObjectType getObjectType() {
		return DBObjectType.JAVA_INNER_CLASS;
	}

	@Override
	protected String initObject(ConnectionHandler connection, DBObject parentObject, DBJavaInnerClassMetadata metadata) throws SQLException {
		this.kind = DBJavaClassKind.get(metadata.getObjectKind());
		this.accessibility = DBJavaAccessibility.get(metadata.getAccessibility());

		set(FINAL, metadata.isFinal());
		set(ABSTRACT, metadata.isAbstract());
		set(STATIC, metadata.isStatic());

		return metadata.getObjectName();
	}

	@Override
	public String getPresentableText() {
		return presentableNameCache.computeIfAbsent(this, o -> o.getName().substring(o.getName().indexOf('$') + 1));
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
}
