package com.dci.intellij.dbn.common.count;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class Counter {
    private final CounterType type;
    private final Set<CounterListener> listeners = new HashSet<>();
    private final AtomicInteger count = new AtomicInteger(0);

    public Counter(CounterType type) {
        this.type = type;
    }

    public CounterType getType() {
        return type;
    }

    public int increment() {
        int value = count.incrementAndGet();
        listeners.forEach(l -> l.when(value));
        return value;
    }

    public int decrement() {
        int value = count.decrementAndGet();
        listeners.forEach(l -> l.when(value));
        return value;
    }

    public int get() {
        return count.get();
    }

    public void set(int count) {
        this.count.set(count);
    }

    public void reset() {
        set(0);
    }

    @Override
    public String toString() {
        return count.toString();
    }

    public void addListener(CounterListener listener) {
        listeners.add(listener);
    }
}