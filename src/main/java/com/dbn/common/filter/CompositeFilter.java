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

package com.dbn.common.filter;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode
public class CompositeFilter<T> implements Filter<T>{
    private final Filter<T> filter1;
    private final Filter<T> filter2;

    public CompositeFilter(Filter<T> filter1, Filter<T> filter2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    /**
     * Creates a composite "AND" filter from the two filters given as parameter
     * Any of the two filters can be null, the composite only considers the non-null input filters
     * If both filters are null, the returned composite will be an "accept all" filter
     *
     * @param filter1 the first filter
     * @param filter2 the second filter
     * @return a {@link Filter} object
     * @param <T> the type of the entities being filtered
     */
    @NotNull
    public static <T> Filter<T> from(@Nullable Filter<T> filter1, @Nullable Filter<T> filter2) {
        if (filter1 != null && filter2 != null) return new CompositeFilter<>(filter1, filter2);
        if (filter1 != null) return filter1;
        if (filter2 != null) return filter2;
        return o -> true;
    }

    @Override
    public final boolean accepts(T object) {
        return filter1.accepts(object) && filter2.accepts(object);
    }
}
