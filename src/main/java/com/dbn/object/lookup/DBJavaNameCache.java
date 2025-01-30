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

package com.dbn.object.lookup;

import com.dbn.common.collections.ConcurrentStringInternMap;
import lombok.experimental.UtilityClass;

/**
 * Utility class for managing transformations and caching of database-style Java class names.
 * Provides methods to compute and cache simple names and canonical Java class names for efficient reuse.
 *
 * @author Dan Cioca (Oracle)
 */
@UtilityClass
public class DBJavaNameCache {
    private static final ConcurrentStringInternMap simpleNameCache = new ConcurrentStringInternMap();
    private static final ConcurrentStringInternMap canonicalNameCache = new ConcurrentStringInternMap();

    /**
     * Extracts and returns the simple name from the given database-style Java class name.
     * The simple name is the part of the class name after the last "/".
     * The result is cached for future invocations.
     *
     * @param objectName the database-style Java class name, where segments are separated by "/"
     * @return the simple name of the class, which is the part of the name after the last "/"
     */
    public static String getSimpleName(String objectName) {
        return simpleNameCache.computeIfAbsent(objectName, n -> n.substring(n.lastIndexOf("/") + 1));
    }

    /**
     * Transforms a database representation of a Java class name into its canonical Java class name.
     * The method replaces all occurrences of "/" with "." in the input string.
     * The result is cached for future invocations.
     *
     * @param objectName the database representation of the Java class name, where parts of the name
     *                   are separated by "/" instead of "."
     * @return the canonical Java class name with parts of the name separated by "."
     */
    public static String getCanonicalName(String objectName) {
        return canonicalNameCache.computeIfAbsent(objectName, n -> n.replace("/", "."));
    }
}
