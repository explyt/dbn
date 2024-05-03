package com.dbn.editor.session.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.session.SessionBrowser;
import com.dbn.editor.session.SessionBrowserFilter;
import com.dbn.editor.session.model.SessionBrowserModel;
import com.dbn.editor.session.options.SessionBrowserSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class ClearFiltersAction extends AbstractSessionBrowserAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            sessionBrowser.clearFilter();
            SessionBrowserSettings sessionBrowserSettings = sessionBrowser.getSettings();
            if (sessionBrowserSettings.isReloadOnFilterChange()) {
                sessionBrowser.loadSessions(false);
            } else {
                sessionBrowser.refreshTable();
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Clear Filter");
        presentation.setIcon(Icons.DATASET_FILTER_CLEAR);

        boolean enabled = false;
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            SessionBrowserModel tableModel = sessionBrowser.getTableModel();
            if (tableModel != null) {
                SessionBrowserFilter filter = tableModel.getFilter();
                if (filter != null) {
                    enabled = !filter.isEmpty();
                }
            }
        }

        presentation.setEnabled(enabled);

    }
}