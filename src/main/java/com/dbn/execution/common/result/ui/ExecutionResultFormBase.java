package com.dbn.execution.common.result.ui;

import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.execution.ExecutionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExecutionResultFormBase<T extends ExecutionResult<?>> extends DBNFormBase implements ExecutionResultForm<T>{
    private T executionResult;

    public ExecutionResultFormBase(@NotNull T executionResult) {
        super(null, executionResult.getProject());
        this.executionResult = executionResult;
    }

    @NotNull
    @Override
    public final T getExecutionResult() {
        return Failsafe.nn(executionResult);
    }

    @Override
    public void setExecutionResult(@NotNull T executionResult) {
        if (this.executionResult != executionResult) {
            this.executionResult = Disposer.replace(this.executionResult, executionResult);
            this.executionResult.setPrevious(null);
            rebuildForm();
        }
    }

    protected void rebuildForm(){}

    @Override
    public void disposeInner() {
        Disposer.dispose(executionResult);
        executionResult = null;
        super.disposeInner();
    }
}
