package com.dbn.browser.model;

import com.dbn.common.event.ProjectEvents;
import com.dbn.common.ui.tree.TreeEventType;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionHandlerStatusListener;
import org.jetbrains.annotations.NotNull;

public class ConnectionBrowserTreeModel extends BrowserTreeModel {
    public ConnectionBrowserTreeModel(ConnectionHandler connection) {
        super(connection.getObjectBundle());
        ProjectEvents.subscribe(connection.getProject(), this, ConnectionHandlerStatusListener.TOPIC, connectionHandlerStatusListener());
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return getConnection() == node.getConnection();
    }

    public ConnectionHandler getConnection() {
        return getRoot().getConnection();
    }

    @NotNull
    private ConnectionHandlerStatusListener connectionHandlerStatusListener() {
        return (connectionId) -> {
            ConnectionHandler connection = getConnection();
            if (connection.getConnectionId() == connectionId) {
                notifyListeners(connection.getObjectBundle(), TreeEventType.NODES_CHANGED);
            }
        };
    }
}
