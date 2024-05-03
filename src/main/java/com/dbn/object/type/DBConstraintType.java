package com.dbn.object.type;

import com.dbn.common.constant.Constant;
import lombok.Getter;

@Getter
public enum DBConstraintType implements Constant<DBConstraintType> {
    CHECK("check"),
    DEFAULT("default"),
    UNKNOWN("unknown"),
    PRIMARY_KEY("primary key"),
    FOREIGN_KEY("foreign key"),
    UNIQUE_KEY("unique key"),
    VIEW_CHECK("view check"),
    VIEW_READONLY("view readonly");

    private final String name;

    DBConstraintType(String name) {
        this.name = name;
    }
}
