package com.dbn.browser.action;

import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.common.action.Lookups;
import com.dbn.common.action.ToggleAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class AutoscrollToEditorAction extends ToggleAction implements DumbAware{

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        Project project = Lookups.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            return browserManager.getAutoscrollToEditor().value();
        }
        return false;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Project project = Lookups.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            browserManager.getAutoscrollToEditor().setValue(state);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("Autoscroll to Editor");
    }
}
