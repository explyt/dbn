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
import com.dbn.object.type.DBJavaClassType;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;

import java.util.List;

import static com.dbn.object.type.DBJavaClassType.EXCEPTION;

@Getter
public class JavaFactoryInput extends ObjectFactoryInput{
    private final DBObjectRef<DBSchema> schema;
    private final String packageName;
    private final String className;
    private final DBJavaClassType classType;
    private String extendsSuffix = " ";

    public JavaFactoryInput(DBSchema schema, String packageName, String objectName, DBJavaClassType javaType, DBObjectType methodType, int index) {
        super(objectName, methodType, null, index);
        this.schema = DBObjectRef.of(schema);
        this.packageName = packageName;
        this.className = objectName;
        this.classType = javaType;

        if(javaType == EXCEPTION){
            this.extendsSuffix = " extends Exception ";
        }
    }

    public String getTypeIdentifier() {
        switch (classType) {
            case INTERFACE: return "interface";
            case ANNOTATION: return "@interface";
            case RECORD: return "record";
            case ENUM: return "enum";
            default: return "class";
        }
    }

    @Override
    public void validate(List<String> errors) {

    }
}
