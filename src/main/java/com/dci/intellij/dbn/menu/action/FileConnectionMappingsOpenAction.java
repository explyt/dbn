package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class FileConnectionMappingsOpenAction extends DumbAwareProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        FileConnectionContextManager executionManager = FileConnectionContextManager.getInstance(project);
        executionManager.openFileConnectionMappings();
    }
}