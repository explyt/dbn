package com.dbn.common.index;


import gnu.trove.map.hash.TShortObjectHashMap;

public class IndexRegistry<T extends Indexable> {
    private final TShortObjectHashMap<T> INDEX = new TShortObjectHashMap<>();

    public void add(T element) {
        INDEX.put(element.index(), element);
    }

    public T get(short index) {
        return INDEX.get(index);
    }

    public int size() {
        return INDEX.size();
    }
}
