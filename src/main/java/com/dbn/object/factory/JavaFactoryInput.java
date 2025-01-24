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

package com.dbn.object.factory;

import com.dbn.object.DBSchema;
import com.dbn.object.lookup.DBObjectRef;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;

import java.util.List;

@Getter
public class JavaFactoryInput extends ObjectFactoryInput{
    private final DBObjectRef<DBSchema> schema;
    private final String packageName;
    private final String className;
    private final String javaType;

    public JavaFactoryInput(DBSchema schema, String packageName, String objectName, String javaType, DBObjectType methodType, int index) {
        super(objectName, methodType, null, index);
        this.schema = DBObjectRef.of(schema);
        this.packageName = packageName;
        this.className = objectName;
        if(javaType.equals("Annotation")){
            javaType = "@interface";
        }
        this.javaType = javaType;
    }

    @Override
    public void validate(List<String> errors) {
        String fullyQualifiedName;
        if(packageName.isEmpty()){
            fullyQualifiedName = className;
        } else {
            fullyQualifiedName = packageName + "." + className;
        }

        if (className.isEmpty()) {
            errors.add("Class name is not specified");
        } else if (fullyQualifiedName.contains(" ")) {
            errors.add("No Space allowed");
        }else if (fullyQualifiedName.length() > 30) {
            errors.add("Total length cannot exceed 30 characters");
        }
    }
}
