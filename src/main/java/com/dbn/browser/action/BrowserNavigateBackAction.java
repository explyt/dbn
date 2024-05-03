package com.dbn.browser.action;

import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.browser.ui.DatabaseBrowserTree;
import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class BrowserNavigateBackAction extends ProjectAction {

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {

        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        DatabaseBrowserTree activeTree = browserManager.getActiveBrowserTree();
        boolean enabled = activeTree != null && activeTree.getNavigationHistory().hasPrevious();

        Presentation presentation = e.getPresentation();
        presentation.setText("Back");
        presentation.setIcon(Icons.BROWSER_BACK);
        presentation.setEnabled(enabled);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
        if (activeBrowserTree != null) {
            activeBrowserTree.navigateBack();
        }
    }
}
