package com.dbn.browser.action;

import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.common.action.Lookups;
import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.DatabaseFeature;
import com.dbn.editor.session.SessionBrowserManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SessionBrowserOpenAction extends ProjectAction {
    private static ConnectionHandler getConnection(@NotNull AnActionEvent e) {
        Project project = Lookups.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            return browserManager.getActiveConnection();
        }
        return null;
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        ConnectionHandler connection = getConnection(e);
        if (connection != null) {
            presentation.setEnabled(true);
            presentation.setVisible(DatabaseFeature.SESSION_BROWSING.isSupported(connection));
        } else {
            presentation.setVisible(false);
            presentation.setEnabled(false);
        }
        presentation.setText("Open Session Browser");
        presentation.setIcon(Icons.SESSION_BROWSER);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ConnectionHandler connection = getConnection(e);
        if (connection != null) {
            SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(project);
            sessionBrowserManager.openSessionBrowser(connection);
        }

    }

}
