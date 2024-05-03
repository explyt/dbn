package com.dbn.execution.method;

import com.dbn.data.type.DBDataType;
import com.dbn.object.DBArgument;
import com.dbn.object.DBTypeAttribute;
import com.dbn.object.lookup.DBObjectRef;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.Objects;

@Getter
@Setter
public class ArgumentValue {
    private final DBObjectRef<DBArgument> argumentRef;
    private DBObjectRef<DBTypeAttribute> attributeRef;
    private ArgumentValueHolder valueHolder;

    public ArgumentValue(@NotNull DBArgument argument, @Nullable DBTypeAttribute attribute, ArgumentValueHolder valueHolder) {
        this.argumentRef = DBObjectRef.of(argument);
        this.attributeRef = DBObjectRef.of(attribute);
        this.valueHolder = valueHolder;
    }

    public ArgumentValue(@NotNull DBArgument argument, ArgumentValueHolder valueHolder) {
        this.argumentRef = DBObjectRef.of(argument);
        this.valueHolder = valueHolder;
    }

    @Nullable
    public DBArgument getArgument() {
        return argumentRef.get();
    }

    public DBTypeAttribute getAttribute() {
        return DBObjectRef.get(attributeRef);
    }

    public String getName() {
        return
            attributeRef == null ?
            argumentRef.getObjectName() :
            argumentRef.getObjectName() + '.' + attributeRef.getObjectName();
    }

    public Object getValue() {
        return valueHolder.getValue();
    }

    public boolean isLargeObject() {
        DBArgument argument = getArgument();
        if (argument == null) return false;

        DBDataType dataType = argument.getDataType();
        return dataType.isNative() && dataType.getNativeType().isLargeObject();
    }

    public  boolean isLargeValue() {
        Object value = valueHolder.getValue();
        if (value == null) return false;

        if (value instanceof String) {
            String stringValue = (String) value;
            return stringValue.length() > 200 || stringValue.contains("\n");
        }

        return false;
    }

    public boolean matches(DBArgument argument) {
        return Objects.equals(argument.ref(), this.argumentRef);
    }

    public boolean matches(DBTypeAttribute attribute) {
        return Objects.equals(attribute.ref(), this.attributeRef);
    }

    public boolean isCursor() {
        return getValue() instanceof ResultSet;
    }

    public void setValue(Object value) {
        valueHolder.setValue(value);
    }

    public String toString() {
        return argumentRef.getObjectName() + " = " + getValue();
    }

    public static <T> ArgumentValueHolder<T> createBasicValueHolder(T value) {
        ArgumentValueHolder<T> valueStore = new ArgumentValueHolder<T>() {
            private T value;

            @Override
            public T getValue() {
                return value;
            }

            @Override
            public void setValue(T value) {
                this.value = value;
            }
        };

        valueStore.setValue(value);
        return valueStore;
    }
}
