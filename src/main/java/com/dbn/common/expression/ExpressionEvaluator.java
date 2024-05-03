package com.dbn.common.expression;

public interface ExpressionEvaluator {

    boolean verifyExpression(String expression, ExpressionEvaluatorContext context);

    boolean verifyExpression(String expression, ExpressionEvaluatorContext context, Class<?> expectedOutcome);

    boolean evaluateBooleanExpression(String expression, ExpressionEvaluatorContext context);

    <T> T evaluateExpression(String expression, ExpressionEvaluatorContext context);
}
