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

package com.dbn.object;

import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.type.DBJavaAccessibility;
import com.dbn.object.type.DBJavaClassKind;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public interface DBJavaClass extends DBSchemaObject {

	@NotNull
	String getName();

	String getSimpleName();

	String getCanonicalName();

	DBJavaClassKind getKind();

	DBJavaAccessibility getAccessibility();

	boolean isFinal();

	boolean isAbstract();

	boolean isStatic();

	boolean isInner();

	boolean isPrimitive();

    List<DBJavaMethod> getMethods();

	List<DBJavaMethod> getStaticMethods();

    DBJavaMethod getMethod(String name);

	List<DBJavaField> getFields();

	DBJavaField getField(String name);

	DBJavaClass getOuterClass();

	List<DBJavaClass> getInnerClasses();

	DBJavaClass getInnerClass(String name);
}
