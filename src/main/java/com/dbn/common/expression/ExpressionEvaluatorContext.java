package com.dbn.common.expression;

import com.dbn.common.util.Strings;
import lombok.Getter;
import lombok.Setter;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ExpressionEvaluatorContext {
    private final Map<String, Object> bindVariables = new HashMap<>();
    private boolean temporary;
    private String expression;
    private Throwable error;

    public ExpressionEvaluatorContext(Map<String, Object> bindVariables) {
        this.bindVariables.putAll(bindVariables);
    }

    public void addBindVariable(String key, Object value) {
        bindVariables.put(key, value);
    }

    public ScriptContext createScriptContext() {
        ScriptContext scriptContext = new SimpleScriptContext();
        bindVariables.forEach((k, v) -> scriptContext.setAttribute(k, v, ScriptContext.ENGINE_SCOPE));
        return scriptContext;
    }

    public boolean isValid() {
        return error == null && Strings.isNotEmptyOrSpaces(expression);
    }
}
