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

package com.dbn.object.type;

import com.dbn.common.constant.Constant;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.Presentable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NonNls;

import javax.swing.Icon;

@NonNls
@Getter
@AllArgsConstructor
public enum DBJavaClassType implements Constant<DBJavaClassType>, Presentable {
    CLASS     ("Class",      Icons.DBO_JAVA_CLASS),
    INTERFACE ("Interface",  Icons.DBO_JAVA_INTERFACE),
//    RECORD    ("Record",     Icons.DBO_JAVA_RECORD),
    ENUM      ("Enum",       Icons.DBO_JAVA_ENUMERATION),
    ANNOTATION("Annotation", Icons.DBO_JAVA_ANNOTATION),
    EXCEPTION ("Exception",  Icons.DBO_JAVA_EXCEPTION);

    private final String name;
    private final Icon icon;
}
