package com.dbn.debugger.jdbc.evaluation;

import com.dbn.language.psql.PSQLFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DBJdbcDebuggerEditorsProvider extends XDebuggerEditorsProvider {
    public static final DBJdbcDebuggerEditorsProvider INSTANCE = new DBJdbcDebuggerEditorsProvider();

    private DBJdbcDebuggerEditorsProvider(){}

    @NotNull
    @Override
    public FileType getFileType() {
        return PSQLFileType.INSTANCE;
    }

    @Override
    public @NotNull Document createDocument(@NotNull Project project, @NotNull XExpression expression, @Nullable XSourcePosition sourcePosition, @NotNull EvaluationMode mode) {
        return new DocumentImpl(expression.getExpression());
    }
}
