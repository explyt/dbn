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

package com.dbn.data.editor.ui;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Supplier;

@Getter
@Setter
public abstract class ListPopupValuesProviderBase implements ListPopupValuesProvider{
    private final String name;
    private boolean loaded = true;

    public ListPopupValuesProviderBase(String name) {
        this.name = name;
    }

    public ListPopupValuesProviderBase(String name, boolean loaded) {
        this.name = name;
        this.loaded = loaded;
    }

    public static ListPopupValuesProviderBase create(String name, Supplier<List<String>> valueProvider) {
        return new ListPopupValuesProviderBase(name, false) {
            @Override
            public List<String> getValues() {
                return valueProvider.get();
            }
        };
    }
}
