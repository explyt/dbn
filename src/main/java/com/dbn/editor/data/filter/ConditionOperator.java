package com.dbn.editor.data.filter;

import com.dbn.common.ui.Presentable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;

@Getter
public enum ConditionOperator implements Presentable {
    EQUAL("=", false),
    NOT_EQUAL("!=", false),
    LESS("<", false),
    GREATER(">", false),
    LESS_EQUAL("<=", false),
    GREATER_EQUAL(">=", false),
    LIKE("like", false),
    NOT_LIKE("not like", false),
    BETWEEN("between", false),
    IN("in", false),
    NOT_IN("not in", false),
    IS_NULL("is null", true),
    IS_NOT_NULL("is not null", true);

    public static final ConditionOperator[] ALL_CONDITION_OPERATORS = new ConditionOperator[] {
            EQUAL,
            NOT_EQUAL,
            LESS,
            GREATER,
            LESS_EQUAL,
            GREATER_EQUAL,
            LIKE,
            NOT_LIKE,
            BETWEEN,
            IN,
            NOT_IN,
            IS_NULL,
            IS_NOT_NULL};

    public static final ConditionOperator[] NUMBER_CONDITION_OPERATORS = new ConditionOperator[] {
            EQUAL,
            NOT_EQUAL,
            LESS,
            GREATER,
            LESS_EQUAL,
            GREATER_EQUAL,
            BETWEEN,
            IN,
            NOT_IN,
            IS_NULL,
            IS_NOT_NULL};

    public static final ConditionOperator[] DATE_CONDITION_OPERATORS = new ConditionOperator[] {
            EQUAL,
            NOT_EQUAL,
            LESS,
            GREATER,
            LESS_EQUAL,
            GREATER_EQUAL,
            BETWEEN,
            IN,
            NOT_IN,
            IS_NULL,
            IS_NOT_NULL};

    public static final ConditionOperator[] STRING_CONDITION_OPERATORS = new ConditionOperator[] {
            EQUAL,
            NOT_EQUAL,
            LIKE,
            NOT_LIKE,
            IN,
            NOT_IN,
            IS_NULL,
            IS_NOT_NULL};


    public static ConditionOperator[] getConditionOperators (Class dataTypeClass) {
        if (dataTypeClass != null) {
            if (String.class.isAssignableFrom(dataTypeClass)) {
                return STRING_CONDITION_OPERATORS;
            }

            if (Number.class.isAssignableFrom(dataTypeClass)) {
                return NUMBER_CONDITION_OPERATORS;
            }

            if (Date.class.isAssignableFrom(dataTypeClass)) {
                return DATE_CONDITION_OPERATORS;
            }
        }

        return ALL_CONDITION_OPERATORS;
    }

    private final String text;
    private final boolean terminal;

    ConditionOperator(String text, boolean terminal) {
        this.text = text;
        this.terminal = terminal;
    }

    public String toString() {
        return text;
    }

    @NotNull
    @Override
    public String getName() {
        return text;
    }

    public static ConditionOperator get(String text) {
        for (ConditionOperator operator : ConditionOperator.values()) {
            if (Objects.equals(operator.text, text)) return operator;
        }

        return null;
    }
}
