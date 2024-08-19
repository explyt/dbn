package com.dbn.connection.jdbc;

public abstract class IncrementalResourceStatusAdapter<T extends Resource> extends IncrementalStatusAdapter<T, ResourceStatus>{
    private IncrementalResourceStatusAdapter(T resource, ResourceStatus status) {
        super(resource, status);
    }

    @Override
    protected void statusChanged() {
        T resource = getResource();
        ResourceStatus status = getStatus();
        resource.statusChanged(status);
    }

    public static <T extends Resource> IncrementalResourceStatusAdapter<T> create(T resource, ResourceStatus status, Setter setter){
        return new IncrementalResourceStatusAdapter<>(resource, status) {
            @Override
            protected boolean setInner(ResourceStatus status, boolean value) {
                return setter.set(status, value);
            }
        };
    }

    @FunctionalInterface
    public interface Setter {
        boolean set(ResourceStatus status, boolean value);
    }
}
