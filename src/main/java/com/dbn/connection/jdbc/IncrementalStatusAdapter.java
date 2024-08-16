package com.dbn.connection.jdbc;

import com.dbn.common.property.Property;
import com.dbn.common.property.PropertyHolder;
import com.dbn.common.ref.WeakRef;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class IncrementalStatusAdapter<T extends PropertyHolder<P>, P extends Property.IntBase> {
    private final @Getter P status;
    private final WeakRef<T> resource;
    private final Lock lock = new ReentrantLock();
    private final AtomicInteger counter = new AtomicInteger();

    public IncrementalStatusAdapter(T resource, P status) {
        this.status = status;
        this.resource = WeakRef.of(resource);
    }

    public final boolean set(boolean value) {
        try {
            lock.lock();
            return change(value);
        } finally {
            lock.unlock();
        }
    }

    private boolean change(boolean value) {
        int current = value ?
                counter.incrementAndGet() :
                counter.decrementAndGet();

        boolean changed = setInner(status, current > 0);
        if (changed) statusChanged();
        return changed;
    }

    public T getResource() {
        return resource.ensure();
    }

    protected abstract void statusChanged();

    protected abstract boolean setInner(P status, boolean value);
}
