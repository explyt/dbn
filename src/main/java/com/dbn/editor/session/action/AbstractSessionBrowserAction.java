package com.dbn.editor.session.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.action.DataKeys;
import com.dbn.common.action.Lookups;
import com.dbn.editor.session.SessionBrowser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSessionBrowserAction extends BasicAction {

    @Nullable
    public static SessionBrowser getSessionBrowser(AnActionEvent e) {
        SessionBrowser sessionBrowser = e.getData((DataKeys.SESSION_BROWSER));
        if (sessionBrowser == null) {
            FileEditor fileEditor = Lookups.getFileEditor(e);
            if (fileEditor instanceof SessionBrowser) {
                return (SessionBrowser) fileEditor;
            }
        } else {
            return sessionBrowser;
        }
        return null;
    }
}
