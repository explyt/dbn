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

package com.dbn.execution.common.ui;

import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.AutoCommitLabel;
import com.dbn.common.ui.ValueSelectorOption;
import com.dbn.common.ui.form.DBNCollapsibleForm;
import com.dbn.common.ui.form.DBNForm;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.misc.DBNComboBox;
import com.dbn.common.ui.util.Listeners;
import com.dbn.common.util.Strings;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionType;
import com.dbn.connection.SchemaId;
import com.dbn.connection.SessionId;
import com.dbn.connection.session.DatabaseSession;
import com.dbn.connection.session.DatabaseSessionBundle;
import com.dbn.database.DatabaseFeature;
import com.dbn.database.interfaces.DatabaseCompatibilityInterface;
import com.dbn.debugger.DBDebuggerType;
import com.dbn.execution.ExecutionOption;
import com.dbn.execution.ExecutionOptions;
import com.dbn.execution.LocalExecutionInput;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.List;

public class ExecutionOptionsForm extends DBNFormBase implements DBNCollapsibleForm {
    private JPanel mainPanel;
    private JPanel timeoutPanel;
    private JLabel connectionLabel;
    private JLabel targetSchemaLabel;
    private JLabel targetSessionLabel;
    private JCheckBox commitCheckBox;
    private JCheckBox reuseVariablesCheckBox;
    private JCheckBox enableLoggingCheckBox;
    private DBNComboBox<SchemaId> targetSchemaComboBox;
    private DBNComboBox<DatabaseSession> targetSessionComboBox;
    private AutoCommitLabel autoCommitLabel;

    private final LocalExecutionInput executionInput;
    private final Listeners<ChangeListener> changeListeners = Listeners.create(this);
    private final DBDebuggerType debuggerType;

    public ExecutionOptionsForm(DBNForm parent, LocalExecutionInput executionInput, @NotNull DBDebuggerType debuggerType) {
        super(parent);
        this.executionInput = executionInput;
        this.debuggerType = debuggerType;

        ConnectionHandler connection = executionInput.ensureConnection();

        if (isSchemaSelectionAllowed()) {
            //ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, new SetExecutionSchemaComboBoxAction(executionInput));

            targetSchemaComboBox.setValues(connection.getSchemaIds());
            targetSchemaComboBox.setSelectedValue(executionInput.getTargetSchemaId());
            targetSchemaComboBox.set(ValueSelectorOption.HIDE_DESCRIPTION, true);
            targetSchemaComboBox.addActionListener(actionListener);
            targetSchemaLabel.setVisible(false);
        } else {
            targetSchemaComboBox.setVisible(false);
            targetSchemaLabel.setVisible(true);
            SchemaId targetSchema = executionInput.getTargetSchemaId();
            if (targetSchema == null) {
                targetSessionLabel.setText("No schema selected");
                targetSessionLabel.setIcon(Icons.DBO_SCHEMA);
            } else {
                targetSchemaLabel.setText(targetSchema.id());
                targetSchemaLabel.setIcon(Icons.DBO_SCHEMA);
            }
        }

        DatabaseSessionBundle sessionBundle = connection.getSessionBundle();
        SessionId targetSessionId = executionInput.getTargetSessionId();
        if (isSessionSelectionAllowed()) {
            DatabaseSession targetSession = sessionBundle.getSession(targetSessionId);
            List<DatabaseSession> sessions = sessionBundle.getSessions(ConnectionType.MAIN, ConnectionType.POOL, ConnectionType.SESSION);

            targetSessionComboBox.setValues(sessions);
            targetSessionComboBox.setSelectedValue(targetSession);
            targetSessionComboBox.set(ValueSelectorOption.HIDE_DESCRIPTION, true);
            targetSessionComboBox.addActionListener(actionListener);
            targetSessionLabel.setVisible(false);
        } else {
            targetSessionComboBox.setVisible(false);
            targetSessionLabel.setVisible(true);
            targetSessionId = debuggerType == DBDebuggerType.NONE ? targetSessionId : SessionId.DEBUG;
            DatabaseSession targetSession = sessionBundle.getSession(targetSessionId);
            targetSessionLabel.setText(targetSession.getName());
            targetSessionLabel.setIcon(targetSession.getIcon());
        }

        connectionLabel.setText(connection.getName());
        connectionLabel.setIcon(connection.getIcon());
        autoCommitLabel.init(getProject(), null, connection, targetSessionId);

        ExecutionOptions options = executionInput.getOptions();
        commitCheckBox.setSelected(options.is(ExecutionOption.COMMIT_AFTER_EXECUTION));
        commitCheckBox.setEnabled(!connection.isAutoCommit());

        commitCheckBox.addActionListener(actionListener);

        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connection) && executionInput.isDatabaseLogProducer()) {
            enableLoggingCheckBox.setEnabled(!debuggerType.isDebug());
            enableLoggingCheckBox.setSelected(!debuggerType.isDebug() && options.is(ExecutionOption.ENABLE_LOGGING));
            DatabaseCompatibilityInterface compatibility = connection.getCompatibilityInterface();
            String databaseLogName = compatibility.getDatabaseLogName();
            if (Strings.isNotEmpty(databaseLogName)) {
                enableLoggingCheckBox.setText("Enable logging (" + databaseLogName + ")");
            }
        } else{
            enableLoggingCheckBox.setVisible(false);
        }

        Disposer.register(this, autoCommitLabel);

        ExecutionTimeoutForm timeoutForm = new ExecutionTimeoutForm(this, executionInput, debuggerType) {
            @Override
            protected void handleChange(boolean hasError) {
                super.handleChange(hasError);
            }
        };

        timeoutPanel.add(timeoutForm.getComponent(), BorderLayout.CENTER);

        reuseVariablesCheckBox.setVisible(executionInput.hasExecutionVariables());
    }

    @Override
    public String getCollapsedTitle() {
        return "Target context: ";
    }

    @Override
    public String getCollapsedTitleDetail() {
        String connectionToken = getConnection().getName();
        LocalExecutionInput executionInput = getExecutionInput();
        SchemaId schemaId = executionInput.getTargetSchemaId();
        String sessionName = executionInput.getTargetSessionName();

        String schemaToken = schemaId == null ? "" : (" / " + schemaId);
        String sessionToken = sessionName == null ? "" : (" / " + sessionName);
        return connectionToken + schemaToken + sessionToken;
    }

    @Override
    public String getExpandedTitle() {
        return "Target context";
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public JCheckBox getReuseVariablesCheckBox() {
        return reuseVariablesCheckBox;
    }

    public void updateExecutionInput() {
        ExecutionOptions options = executionInput.getOptions();
        //options.setUsePoolConnection(usePoolConnectionCheckBox.isSelected());
        options.set(ExecutionOption.COMMIT_AFTER_EXECUTION, commitCheckBox.isSelected());
        options.set(ExecutionOption.ENABLE_LOGGING, enableLoggingCheckBox.isSelected());

        if (isSchemaSelectionAllowed()) {
            SchemaId schema = targetSchemaComboBox.getSelectedValue();
            executionInput.setTargetSchemaId(schema);
        }

        if (isSessionSelectionAllowed()) {
            DatabaseSession targetSession = targetSessionComboBox.getSelectedValue();
            executionInput.setTargetSession(targetSession);
        }
    }

    private boolean isSchemaSelectionAllowed() {
        return getExecutionInput().isSchemaSelectionAllowed();
    }

    private boolean isSessionSelectionAllowed() {
        return getExecutionInput().isSessionSelectionAllowed() && debuggerType == DBDebuggerType.NONE;
    }

    @Deprecated
    public void touch() {
        commitCheckBox.setSelected(!commitCheckBox.isSelected());
        commitCheckBox.setSelected(!commitCheckBox.isSelected());
    }

    public LocalExecutionInput getExecutionInput() {
        return Failsafe.nn(executionInput);
    }

    public ConnectionHandler getConnection() {
        ConnectionHandler connection = getExecutionInput().getConnection();
        return Failsafe.nn(connection);
    }

    private final ActionListener actionListener = e -> changeListeners.notify(l -> l.stateChanged(new ChangeEvent(this)));

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    @Override
    public void disposeInner() {
        super.disposeInner();
        changeListeners.clear();
    }
}
