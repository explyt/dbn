package com.dbn.object.filter.custom;

import com.dbn.object.common.DBObject;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.dbn.common.util.Lists.convert;

@Getter
final class ObjectFilterDefinitionImpl<T extends DBObject> implements ObjectFilterDefinition<T> {

    private final DBObjectType objectType;
    private final String sampleExpression;
    private final List<ObjectFilterAttribute> attributes = new ArrayList<>();
    private final Map<String, Function<T, Object>> valueProviders = new LinkedHashMap<>();

    public ObjectFilterDefinitionImpl(DBObjectType objectType, String sampleExpression) {
        this.objectType = objectType;
        this.sampleExpression = sampleExpression;
    }

    @Override
    public List<String> getAttributeNames() {
        return convert(attributes, a -> a.getName());
    }

    @Override
    public Object getAttributeValue(T source, String attribute) {
        var valueProvider = valueProviders.get(attribute);

        return valueProvider == null ? null : valueProvider.apply(source);
    }

    public ObjectFilterDefinitionImpl<T> withAttribute(Class type, String name, String description, Function<T, Object> valueProvider) {
        ObjectFilterAttribute attribute = new ObjectFilterAttribute(type, name, description);
        attributes.add(attribute);
        valueProviders.put(name, valueProvider);
        return this;
    }
}
