package com.dbn.common.expression;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Objects;

import static com.dbn.common.expression.SqlToGroovyExpressionConverter.cachedSqlToGroovy;
import static com.dbn.common.expression.SqlToGroovyExpressionConverter.sqlToGroovy;
import static com.dbn.common.util.Unsafe.cast;

@Slf4j
public class GroovyExpressionEvaluator implements ExpressionEvaluator{
    private final ScriptEngine scriptEngine;

    public GroovyExpressionEvaluator() {
        ScriptEngineManager manager = new ScriptEngineManager();
        scriptEngine = manager.getEngineByName("Groovy");
    }

    @Override
    public boolean verifyExpression(String expression, ExpressionEvaluatorContext context) {
        return verifyExpression(expression, context, null);
    }

    @Override
    public boolean verifyExpression(String expression, ExpressionEvaluatorContext context, Class<?> expectedOutcome) {
        evaluate(expression, context, expectedOutcome, true);
        return context.isValid();
    }

    @Override
    public <T> T evaluateExpression(String expression, ExpressionEvaluatorContext context) {
        try {
            return evaluate(expression, context, null, false);
        } catch (Throwable e) {
            log.error("Failed to evaluate expression: {}", expression, e);
            return null;
        }
    }

    @Override
    public boolean evaluateBooleanExpression(String expression, ExpressionEvaluatorContext context) {
        Object result = evaluateExpression(expression, context);
        return result == null || Objects.equals(result, Boolean.TRUE);
    }

    @SneakyThrows
    private <T> T evaluate(String expression, ExpressionEvaluatorContext context, Class<?> expectedOutcome, boolean silent) {
        try {
            expression = context.isTemporary() ? sqlToGroovy(expression) : cachedSqlToGroovy(expression);
            context.setExpression(expression);
            context.setError(null);

            ScriptContext scriptContext = context.createScriptContext();
            Object result = scriptEngine.eval(expression, scriptContext);

            verifyResult(result, expectedOutcome);
            return cast(result);
        } catch (Throwable e) {
            context.setError(e);
            if (!silent) throw e;
            return null;
        }
    }


    private static void verifyResult(Object result, Class<?> expectedType) {
        if (result == null) return;
        if (expectedType == null) return;
        if (expectedType.isAssignableFrom(result.getClass())) return;

        throw new ClassCastException("Expected " + expectedType + " but got " + result.getClass());
    }
}
