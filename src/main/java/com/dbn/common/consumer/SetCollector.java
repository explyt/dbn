package com.dbn.common.consumer;

import com.dbn.common.util.Commons;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SetCollector<T> implements Consumer<T> {
    private Set<T> elements;

    SetCollector() {}

    public static <T> SetCollector<T> basic() {
        return new SetCollector<>();
    }

    public static <T> SetCollector<T> concurrent() {
        return new SetCollector<>() {
            @Override
            protected Set<T> createSet() {
                return Collections.newSetFromMap(new ConcurrentHashMap<>());
            }
        };
    }

    public static <T> SetCollector<T> linked() {
        return new SetCollector<>() {
            @Override
            protected Set<T> createSet() {
                return new LinkedHashSet<>();
            }
        };
    }

    public static <T> SetCollector<T> sorted(Comparator<T> comparator) {
        return new SetCollector<>() {
            @Override
            protected Set<T> createSet() {
                return new TreeSet<>(comparator);
            }
        };
    }

    @Override
    public void accept(T element) {
        if (elements == null) {
            elements = createSet();
        }
        elements.add(element);
    }

    protected Set<T> createSet() {
        return new HashSet<>();
    }

    public Set<T> elements() {
        return Commons.nvl(elements, Collections.emptySet());
    }

    public boolean isEmpty() {
        return elements == null || elements.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public int size() {
        return elements == null ? 0 : elements.size();
    }

    public void clear() {
        elements.clear();
    }
}
