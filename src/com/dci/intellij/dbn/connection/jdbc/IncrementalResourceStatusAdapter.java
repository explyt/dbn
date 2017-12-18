package com.dci.intellij.dbn.connection.jdbc;

import java.util.concurrent.atomic.AtomicInteger;

import com.dci.intellij.dbn.common.dispose.FailsafeWeakRef;

public abstract class IncrementalResourceStatusAdapter<T extends Resource> {
    private final ResourceStatus status;
    private final FailsafeWeakRef<T> resource;
    private AtomicInteger count = new AtomicInteger();

    public IncrementalResourceStatusAdapter(ResourceStatus status, T resource) {
        this.status = status;
        this.resource = new FailsafeWeakRef<T>(resource);
    }

    public boolean set(ResourceStatus status, boolean value) {
        if (status == this.status) {
            int current = value ?
                    count.incrementAndGet() :
                    count.decrementAndGet();

            boolean changed = setInner(status, current > 0);
            if (changed) resource.get().statusChanged(status);
            return changed;
        } else {
            throw new IllegalArgumentException("Invalid resource status");
        }
    }

    protected abstract boolean setInner(ResourceStatus status, boolean value);


}