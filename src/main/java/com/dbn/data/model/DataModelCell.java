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

package com.dbn.data.model;

import com.dbn.common.dispose.UnlistedDisposable;
import com.dbn.data.editor.ui.UserValueHolder;
import com.dbn.data.value.LargeObjectValue;
import org.jetbrains.annotations.NotNull;

public interface DataModelCell<
        R extends DataModelRow<M, ? extends DataModelCell<?, ?>>,
        M extends DataModel<R, ? extends DataModelCell<?, ?>>>
        extends UnlistedDisposable, UserValueHolder<Object> {

    ColumnInfo getColumnInfo();

    int getIndex();

    @NotNull
    M getModel();

    @NotNull
    R getRow();

    default boolean isLargeValue() {
        return getUserValue() instanceof LargeObjectValue;
    }

    default String getTemporaryUserValue() {
        return null;
    }
}
