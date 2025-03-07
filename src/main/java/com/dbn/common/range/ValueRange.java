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

package com.dbn.common.range;

import lombok.Getter;

@Getter
public class ValueRange<T> extends Range{
    private final T value;

    private ValueRange(T value, int left, int right) {
        super(left, right);
        this.value = value;
    }

    public static <T> ValueRange<T> create(T value, int left, int right) {
        return new ValueRange<>(value, left, right);
    }

    @Override
    public String toString() {
        return value + " " + getLeft() + " - " + getRight();
    }
}
