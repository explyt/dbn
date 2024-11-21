/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.connection.transaction.action;

import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.action.AbstractConnectionAction;
import com.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransactionCommitAction extends AbstractConnectionAction {

    public TransactionCommitAction(ConnectionHandler connection) {
        super("Commit", "Commit connection", Icons.CONNECTION_COMMIT, connection);

    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
        transactionManager.commit(connection, null, false, false, null);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionHandler connection) {
        boolean enabled = connection != null && connection.hasUncommittedChanges();
        boolean visible = connection != null && !connection.isAutoCommit();

        presentation.setEnabled(enabled);
        presentation.setVisible(visible);
    }
}
