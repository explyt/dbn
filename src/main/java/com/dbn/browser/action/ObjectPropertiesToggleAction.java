package com.dbn.browser.action;

import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.common.action.Lookups;
import com.dbn.common.action.ToggleAction;
import com.dbn.common.icon.Icons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ObjectPropertiesToggleAction extends ToggleAction implements DumbAware {
    public ObjectPropertiesToggleAction() {
        super("Show properties", null, Icons.BROWSER_OBJECT_PROPERTIES);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        Project project = Lookups.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            return browserManager.getShowObjectProperties().value();
        }
        return false;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Project project = Lookups.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            browserManager.showObjectProperties(state);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText(isSelected(e) ? "Hide Object Properties" : "Show Object Properties");
    }
}
