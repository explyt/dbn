package com.dbn.connection.action;

import com.dbn.common.action.ContextAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.ConnectionRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractConnectionAction extends ContextAction<ConnectionHandler> {
    private final ConnectionRef connection;

    public AbstractConnectionAction(@NotNull ConnectionHandler connection) {
        this.connection = connection.ref();
    }

    public AbstractConnectionAction(String text, String description, Icon icon, @NotNull ConnectionHandler connection) {
        super(text, description, icon);
        this.connection = connection.ref();
    }

    public ConnectionId getConnectionId() {
        return connection.getConnectionId();
    }

    @Nullable
    public ConnectionHandler getConnection() {
        return ConnectionRef.get(connection);
    }

    @Override
    protected ConnectionHandler getTarget(@NotNull AnActionEvent e) {
        return connection.get();
    }

    @NotNull
    @Override
    public Project getProject() {
        return connection.ensure().getProject();
    }
}

