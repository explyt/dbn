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

import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Collections.unmodifiableMap;

@Getter
public final class DBJavaValueType {
    private static final Class[] REGISTRY = new Class[]{
            boolean.class,
            byte.class,
            char.class,
            double.class,
            float.class,
            int.class,
            long.class,
            short.class,

            Boolean.class,
            Byte.class,
            Character.class,
            Double.class,
            Float.class,
            Integer.class,
            Long.class,
            Short.class,
            String.class,
            Number.class,
            BigDecimal.class,

            AtomicBoolean.class,
            AtomicInteger.class,
            AtomicLong.class,

            //...
    };

    private static final Map<String, DBJavaValueType> nameMappings;
    private static final Map<String, DBJavaValueType> pathMappings;

    static {
        Map<String, DBJavaValueType> nameMap = new HashMap<>();
        Map<String, DBJavaValueType> pathMap = new HashMap<>();
        for (Class<?> type : REGISTRY) {
            nameMap.put(type.getSimpleName(), new DBJavaValueType(type));
            pathMap.put(type.getCanonicalName(), new DBJavaValueType(type));
        }
        nameMappings = unmodifiableMap(nameMap);
        pathMappings = unmodifiableMap(pathMap);
    }

    private final Class<?> type;
    private final String name;
    private final String path;
    private final String canonicalName;

    DBJavaValueType(Class<?> type) {
        this.type = type;
        this.name = type.getSimpleName();
        this.path = type.getCanonicalName().replace(".", "/");
        this.canonicalName = type.getCanonicalName();
    }

    public static DBJavaValueType forName(String name) {
        return nameMappings.get(name);
    }

    public static DBJavaValueType forPath(String path) {
        return pathMappings.get(path);
    }
}

