package com.dbn.diagnostics.action;

import com.dbn.common.action.ContextAction;
import com.dbn.common.action.DataKeys;
import com.dbn.diagnostics.ParserDiagnosticsManager;
import com.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractParserDiagnosticsAction extends ContextAction<ParserDiagnosticsForm> {

    @Override
    protected final ParserDiagnosticsForm getTarget(@NotNull AnActionEvent e) {
        return e.getData(DataKeys.PARSER_DIAGNOSTICS_FORM);
    }


    @NotNull
    protected ParserDiagnosticsManager getManager(@NotNull Project project) {
        return ParserDiagnosticsManager.get(project);
    }
}
