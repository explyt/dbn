package com.dbn.connection.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.action.Lookups;
import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.object.status.ObjectStatusManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ObjectsStatusRefreshAction extends BasicAction {

    private final ConnectionRef connection;

    public ObjectsStatusRefreshAction(ConnectionHandler connection) {
        super("Refresh objects status", "", Icons.ACTION_REFRESH);
        this.connection = connection.ref();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = Lookups.ensureProject(e);
        ObjectStatusManager statusManager = ObjectStatusManager.getInstance(project);
        statusManager.refreshObjectsStatus(connection.get(), null);
    }
}
