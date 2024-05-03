package com.dbn.menu.action;

import com.dbn.common.action.BasicAction;
import com.dbn.connection.DatabaseInterfacesBundle;
import com.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class MetaDataDefinitionReloadAction extends BasicAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseInterfacesBundle.reset();
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setVisible(Diagnostics.isBulkActionsEnabled());
    }

}
