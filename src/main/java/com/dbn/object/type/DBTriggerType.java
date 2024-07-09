package com.dbn.object.type;

import com.dbn.common.constant.Constant;
import lombok.Getter;

@Getter
public enum DBTriggerType implements Constant<DBTriggerType> {
    BEFORE("before"),
    AFTER("after"),
    INSTEAD_OF("instead of"),
    UNKNOWN("unknown");

    private final String name;

    DBTriggerType(String name) {
        this.name = name;
    }
}
