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

package com.dbn.execution.common.message.ui;

import com.dbn.common.action.DataKeys;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.navigation.NavigationInstructions;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.misc.DBNScrollPane;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Actions;
import com.dbn.common.util.Safe;
import com.dbn.connection.config.ConnectionConfigListener;
import com.dbn.execution.common.message.ui.tree.MessagesTree;
import com.dbn.execution.common.ui.ExecutionConsoleForm;
import com.dbn.execution.compiler.CompilerMessage;
import com.dbn.execution.explain.result.ExplainPlanMessage;
import com.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import static com.dbn.common.ui.util.Accessibility.setAccessibleName;
import static com.dbn.common.ui.util.Borderless.markBorderless;

public class ExecutionMessagesForm extends DBNFormBase {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JPanel messagesPanel;
    private DBNScrollPane messagesScrollPane;

    private final MessagesTree messagesTree;

    public ExecutionMessagesForm(ExecutionConsoleForm parent) {
        super(parent);
        messagesPanel.setBorder(Borders.lineBorder(JBColor.border(), 0, 1, 1, 0));
        messagesTree = new MessagesTree(this);
        markBorderless(messagesTree);

        messagesScrollPane.setViewportView(messagesTree);
        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel, false, "DBNavigator.ActionGroup.ExecutionConsoleMessages");
        setAccessibleName(actionToolbar, txt("app.execution.aria.ExecutionMessageActions"));
        actionsPanel.add(actionToolbar.getComponent());

        ProjectEvents.subscribe(
                ConnectionConfigListener.TOPIC,
                ConnectionConfigListener.whenRemoved(id -> Safe.run(messagesTree, t -> t.removeMessages(id))));
    }

    public void resetMessagesStatus() {
        getMessagesTree().resetMessagesStatus();
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage, NavigationInstructions instructions) {
        return getMessagesTree().addExecutionMessage(executionMessage, instructions);
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage, NavigationInstructions instructions) {
        return getMessagesTree().addCompilerMessage(compilerMessage, instructions);
    }

    public TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage, NavigationInstructions instructions) {
        return getMessagesTree().addExplainPlanMessage(explainPlanMessage, instructions);
    }

    public void reset() {
        getMessagesTree().reset();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public void selectMessage(@NotNull CompilerMessage compilerMessage, NavigationInstructions instructions) {
        getMessagesTree().selectCompilerMessage(compilerMessage, instructions);
    }

    public void selectMessage(@NotNull StatementExecutionMessage statementExecutionMessage, NavigationInstructions instructions) {
        getMessagesTree().selectExecutionMessage(statementExecutionMessage, instructions);
    }

    public void expand(TreePath treePath) {
        getMessagesTree().makeVisible(treePath);
    }

    @NotNull
    public MessagesTree getMessagesTree() {
        return Failsafe.nn(messagesTree);
    }

    @Override
    public @Nullable Object getData(@NotNull String dataId) {
        if (DataKeys.MESSAGES_TREE.is(dataId)) return messagesTree;
        return null;
    }
}
