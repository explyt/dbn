package com.dbn.connection.config.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.connection.config.ConnectionBundleSettings;
import com.dbn.connection.config.ui.ConnectionListModel;
import com.dbn.data.sorting.SortDirection;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConnectionsSortAction extends ProjectAction {
    private SortDirection currentSortDirection = SortDirection.ASCENDING;
    private final ConnectionBundleSettings connectionBundleSettings;
    private final JList list;

    public ConnectionsSortAction(JList list, ConnectionBundleSettings connectionBundleSettings) {
        this.list = list;
        this.connectionBundleSettings = connectionBundleSettings;
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Icon icon;
        String text;
        if (currentSortDirection != SortDirection.ASCENDING) {
            icon = Icons.ACTION_SORT_ASC;
            text = "Sort Connections Ascending";
        } else {
            icon = Icons.ACTION_SORT_DESC;
            text = "Sort Connections Descending";
        }
        Presentation presentation = e.getPresentation();
        presentation.setIcon(icon);
        presentation.setText(text);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        currentSortDirection = currentSortDirection == SortDirection.ASCENDING ?
                SortDirection.DESCENDING :
                SortDirection.ASCENDING;

        if (list.getModel().getSize() > 0) {
            Object selectedValue = list.getSelectedValue();
            connectionBundleSettings.setModified(true);
            ConnectionListModel model = (ConnectionListModel) list.getModel();
            model.sort(currentSortDirection);
            list.setSelectedValue(selectedValue, true);
        }
    }
}
