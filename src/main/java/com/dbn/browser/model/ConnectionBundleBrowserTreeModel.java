package com.dbn.browser.model;

import com.dbn.common.event.ProjectEvents;
import com.dbn.common.ui.tree.TreeEventType;
import com.dbn.connection.ConnectionBundle;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionHandlerStatusListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectionBundleBrowserTreeModel extends BrowserTreeModel {
    public ConnectionBundleBrowserTreeModel(@NotNull Project project, @Nullable ConnectionBundle connectionBundle) {
        //super(new ConnectionBundleBrowserTreeRoot(project, connectionBundle));
        super(connectionBundle);
        ProjectEvents.subscribe(project, this, ConnectionHandlerStatusListener.TOPIC, connectionHandlerStatusListener());
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return true;
    }

    @NotNull
    private ConnectionHandlerStatusListener connectionHandlerStatusListener() {
        return (connectionId) -> {
            ConnectionHandler connection = ConnectionHandler.get(connectionId);
            if (connection == null) return;

            notifyListeners(connection.getObjectBundle(), TreeEventType.NODES_CHANGED);
        };
    }
}
