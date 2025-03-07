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

import com.dbn.object.lookup.DBObjectRef;
import com.dbn.object.type.DBJavaAccessibility;

import java.util.List;


public interface DBJavaMethod extends DBOrderedObject {

	DBJavaClass getOwnerClass();

	String getOwnerClassName();

    DBJavaClass getReturnClass();

	DBObjectRef<DBJavaClass> getReturnClassRef();

	String getReturnClassName();

	short getReturnArrayDepth();

	DBJavaAccessibility getAccessibility();

	boolean isFinal();

	boolean isStatic();

	boolean isExecutable();

	boolean isAbstract();

	String getSignature();

	short getIndex();

	List<DBJavaParameter> getParameters();

	DBJavaParameter getParameter(String name);

	String getSimpleName();
}
