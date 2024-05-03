package com.dbn.execution;

import com.dbn.common.property.Property;

public enum ExecutionOption implements Property.IntBase {
    CONTEXT_EXPANDED,
    ENABLE_LOGGING,
    COMMIT_AFTER_EXECUTION;

    public static final ExecutionOption[] VALUES = values();

    private final IntMasks masks = new IntMasks(this);

    @Override
    public IntMasks masks() {
        return masks;
    }}
