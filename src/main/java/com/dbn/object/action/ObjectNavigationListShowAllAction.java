package com.dbn.object.action;

import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.browser.ui.DatabaseBrowserTree;
import com.dbn.common.action.ProjectAction;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.list.DBObjectNavigationList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

public class ObjectNavigationListShowAllAction extends ProjectAction {
    private final DBObjectNavigationList navigationList;
    private final DBObject parentObject;

    ObjectNavigationListShowAllAction(DBObject parentObject, DBObjectNavigationList navigationList) {
        this.parentObject = parentObject;
        this.navigationList = navigationList;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ObjectNavigationListActionGroup navigationListActionGroup =
                new ObjectNavigationListActionGroup(parentObject, navigationList, true);

        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                navigationList.getName(),
                navigationListActionGroup,
                e.getDataContext(),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true, null, 10);

        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
        if (activeBrowserTree != null) {
            popup.showInCenterOf(activeBrowserTree);
        }
        //popup.show(DatabaseBrowserComponent.getInstance(project).getBrowserPanel().getTree());
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setText("Show All...");
    }
}
