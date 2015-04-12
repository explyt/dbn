package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.intellij.openapi.project.Project;

public interface DBVirtualFile extends ConnectionProvider, Disposable {
    @Nullable
    Project getProject();

    Icon getIcon();

    @NotNull
    ConnectionHandler getConnectionHandler();
}