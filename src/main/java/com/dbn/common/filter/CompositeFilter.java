package com.dbn.common.filter;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class CompositeFilter<T> implements Filter<T>{
    private final Filter<T> filter1;
    private final Filter<T> filter2;

    public CompositeFilter(Filter<T> filter1, Filter<T> filter2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    public static <T> Filter<T> from(Filter<T> filter1, Filter<T> filter2) {
        return new CompositeFilter<>(filter1, filter2);
    }

    @Override
    public final boolean accepts(T object) {
        return filter1.accepts(object) && filter2.accepts(object);
    }
}
