package com.dbn.object.filter;

import com.dbn.common.ui.Presentable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConditionOperator implements Presentable{
    EQUAL("equal", false),
    NOT_EQUAL("not equal", false),
    LIKE("like", true),
    NOT_LIKE("not like", true);

    private final String name;
    private final boolean allowsWildcards;

    public boolean allowsWildcards() {
        return allowsWildcards;
    }

    @Override
    public String toString() {
        return name;
    }

}
