package com.dbn.oracleAI.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of <code>ArrayList</code> that maintain a fixed capacity.
 * During insertion of new elements, if extra free space is needed, FIFO will be applied.
 * @param <E> type of list element
 */
public class FixedSizeList<E> extends ArrayList<E> {
    private final int maxCapacity;

    /**
     * Creates a new fixed sized list
     * @param capacity the fixed capacity
     */
    public FixedSizeList(int capacity) {
        super(capacity);
        this.maxCapacity = capacity;
    }

    /**
     * Must not be called. FixedSizeList(int capacity) must be used
     */
    public FixedSizeList() {
        throw new IllegalArgumentException("a maximum capacity must be provided");
    }

    /**
     * Creates a new fixed sized list.
     * Maximum capacity will be the size of given collection.
     * @param c the initial collection.
     */
    public FixedSizeList(@NotNull Collection c) {
        super(c);
        this.maxCapacity = c.size();
    }

    /**
     * insure some free space by removing items if needed.
     * @param space the needed space
     */
    private void ensureSpace(int space) {
        int s = maxCapacity - this.size() - space;
        while (s++ < 0) {
            this.remove(0);
        }
    }

    @Override
    public boolean add(E o) {
        ensureSpace(1);
        return super.add(o);
    }

    @Override
    public void add(int index, E element) {
        ensureSpace(1);
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection c) {
        ensureSpace(c.size());
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        ensureSpace(c.size());
        return super.addAll(index, c);
    }

    @Override
    public String toString() {
        return "FixedSizeList{" +
                "maxCapacity=" + maxCapacity +
                ", size=" + this.size() +
                super.toString() +
                '}';
    }
}
