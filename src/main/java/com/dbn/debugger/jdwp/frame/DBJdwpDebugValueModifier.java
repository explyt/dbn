package com.dbn.debugger.jdwp.frame;

import com.dbn.common.util.Strings;
import com.intellij.debugger.engine.JavaValueModifier;
import com.intellij.xdebugger.XExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public class DBJdwpDebugValueModifier extends JavaValueModifier {
    private final DBJdwpDebugValue value;

    DBJdwpDebugValueModifier(DBJdwpDebugValue value) {
        super(value.getJavaValue());
        this.value = value;
    }

    @Override
    protected void setValueImpl(@NotNull XExpression expression, @NotNull XModificationCallback callback) {

    }

    @Override
    public void setValue(@NotNull XExpression expr, @NotNull XModificationCallback callback) {
        String expression = expr.getExpression();
        try {
            if (Strings.isNotEmpty(expression)) {
                while (expression.charAt(0) == '\'') {
                    expression = expression.substring(1);
                }

                while (expression.charAt(expression.length()-1) == '\'') {
                    expression = expression.substring(0, expression.length() -1);
                }
            }

            // TODO DBN-580 implement alternative value modification

            callback.valueModified();
        } catch (Exception e) {
            conditionallyLog(e);
            callback.errorOccurred(e.getMessage());
        }
    }

    @Nullable
    @Override
    public String getInitialValueEditorText() {
        return value == null ? null : value.getValue();
    }
}
