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

package com.dbn.common.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Utility class for validating Java package and class names according to standard naming conventions.
 * This class provides methods to check the validity of package and class names.
 *
 * @author Dan Cioca (Oracle)
 */
@UtilityClass
public class Java {
    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_]([a-zA-Z0-9_]*)(\\.[a-zA-Z_]([a-zA-Z0-9_]*))*$");
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public static boolean isValidPackageName(String packageName) {
        // allow empty package names
        return Strings.isEmpty(packageName) || PACKAGE_NAME_PATTERN.matcher(packageName).matches();
        // TODO disallow keywords and primitives
    }

    public static boolean isValidClassName(String className) {
        return CLASS_NAME_PATTERN.matcher(className).matches();
        // TODO disallow keywords and primitives
    }

    public static String getQualifiedClassName(@Nullable String packageName, String className) {
        return Strings.isEmpty(packageName) ? className : packageName + "." + className;
    }
}
