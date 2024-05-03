package com.dbn.editor.session.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.session.SessionBrowser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class DataFindAction extends AbstractSessionBrowserAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            sessionBrowser.showSearchHeader();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(sessionBrowser != null);
        presentation.setText("Find...");
        presentation.setIcon(Icons.ACTION_FIND);
    }
}