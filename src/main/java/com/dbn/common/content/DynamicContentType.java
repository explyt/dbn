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

package com.dbn.common.content;

import com.dbn.common.constant.Constant;
import org.jetbrains.annotations.NotNull;

public interface DynamicContentType<T extends DynamicContentType<T>> extends Constant<T> {
    default boolean matches(T contentType) {
        return false;
    }

    default T getGenericType() {
        return null;
    };

    DynamicContentType NULL = new DynamicContentType() {
        @Override
        public boolean matches(DynamicContentType contentType) {
            return contentType == this;
        }

        @Override
        public String toString() {
            return "NULL";
        }

        @Override
        public int ordinal() {
            return 0;
        }

        @Override
        public int compareTo(@NotNull Object o) {
            return 0;
        }
    };
}
