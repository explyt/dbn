package com.dbn.common.consumer;

import com.dbn.common.routine.Consumer;
import com.dbn.common.util.Commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListCollector<T> implements Consumer<T> {
    private List<T> elements;

    protected ListCollector() {}

    public static <T> ListCollector<T> basic() {
        return new ListCollector<>();
    }

    public static <T> ListCollector<T> unique() {
        return new ListCollector<>() {
            @Override
            public void accept(T element) {
                if (!elements().contains(element)) {
                    super.accept(element);
                }
            }
        };
    }

    @Override
    public void accept(T element) {
        if (elements == null) {
            elements = createList();
        }
        elements.add(element);
    }

    protected List<T> createList() {
        return new ArrayList<>();
    }

    public List<T> elements() {
        return Commons.nvl(elements, Collections.emptyList());
    }

    public boolean isEmpty() {
        return elements == null || elements.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
