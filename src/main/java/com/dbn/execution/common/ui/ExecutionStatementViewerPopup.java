package com.dbn.execution.common.ui;

import com.dbn.common.ui.StatementViewerPopup;
import com.dbn.execution.ExecutionResult;

public class ExecutionStatementViewerPopup extends StatementViewerPopup {

    public ExecutionStatementViewerPopup(ExecutionResult executionResult) {
        super(
            null/*executionResult.getName()*/,
            executionResult.createPreviewFile(),
            executionResult.getConnection());
    }
}
