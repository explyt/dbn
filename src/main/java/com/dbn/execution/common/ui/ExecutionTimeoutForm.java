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

import com.dbn.common.action.BasicAction;
import com.dbn.common.action.ProjectPopupAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.form.DBNForm;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.util.Actions;
import com.dbn.debugger.DBDebuggerType;
import com.dbn.execution.ExecutionInput;
import com.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

import static com.dbn.common.ui.util.Accessibility.setAccessibleUnit;
import static com.dbn.common.ui.util.TextFields.onTextChange;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public abstract class ExecutionTimeoutForm extends DBNFormBase {
    private JTextField executionTimeoutTextField;
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JLabel hintLabel;

    private boolean hasErrors;
    private transient int timeout;

    private final ExecutionInput executionInput;
    private final DBDebuggerType debuggerType;

    protected ExecutionTimeoutForm(DBNForm parent, ExecutionInput executionInput, DBDebuggerType debuggerType) {
        super(parent);
        this.executionInput = executionInput;
        this.debuggerType = debuggerType;

        timeout = getInputTimeout();
        executionTimeoutTextField.setText(String.valueOf(timeout));
        executionTimeoutTextField.setForeground(timeout == getSettingsTimeout() ?
                UIUtil.getLabelDisabledForeground() :
                UIUtil.getTextFieldForeground());


        onTextChange(executionTimeoutTextField, e -> updateErrorMessage());

        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel, true, new SettingsAction());

        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
    }

    @Override
    protected void initAccessibility() {
        setAccessibleUnit(executionTimeoutTextField, txt("app.shared.unit.Seconds"), txt("app.shared.hint.ZeroForNoTimeout"));
    }

    private void updateErrorMessage() {
        String text = executionTimeoutTextField.getText();
        try {
            timeout = Integer.parseInt(text);
            executionTimeoutTextField.setForeground(timeout == getSettingsTimeout() ?
                    UIUtil.getLabelDisabledForeground() :
                    UIUtil.getTextFieldForeground());

            if (debuggerType.isDebug())
                executionInput.setDebugExecutionTimeout(timeout); else
                executionInput.setExecutionTimeout(timeout);
            hintLabel.setIcon(null);
            hintLabel.setToolTipText(null);
            hasErrors = false;
            handleChange(false);
        } catch (NumberFormatException e1) {
            conditionallyLog(e1);
            //errorLabel.setText("Timeout must be an integer");
            hintLabel.setIcon(Icons.COMMON_ERROR);
            hintLabel.setToolTipText("Timeout must be an integer");
            hasErrors = true;
            handleChange(true);
        }
    }

    private int getInputTimeout() {
        return debuggerType.isDebug() ?
                    executionInput.getDebugExecutionTimeout() :
                    executionInput.getExecutionTimeout();
    }

    private int getSettingsTimeout() {
        ExecutionTimeoutSettings timeoutSettings = executionInput.getExecutionTimeoutSettings();
        return debuggerType.isDebug() ?
                timeoutSettings.getDebugExecutionTimeout() :
                timeoutSettings.getExecutionTimeout();
    }

    protected void handleChange(boolean hasError){}

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public class SettingsAction extends ProjectPopupAction {
        @Override
        public AnAction[] getChildren(AnActionEvent e) {
            return new AnAction[]{
                    new SaveToSettingsAction(),
                    new ReloadDefaultAction()};
        }

        @Override
        protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
            Presentation presentation = e.getPresentation();
            presentation.setText("Settings");
            presentation.setIcon(Icons.ACTION_OPTIONS);
            presentation.setEnabled(!hasErrors && timeout != getSettingsTimeout());
        }
    }

    class SaveToSettingsAction extends BasicAction {
        SaveToSettingsAction() {
            super(txt("app.execution.action.SaveToSettings"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ExecutionTimeoutSettings timeoutSettings = executionInput.getExecutionTimeoutSettings();
            String text = executionTimeoutTextField.getText();
            int timeout = Integer.parseInt(text);

            if (debuggerType.isDebug())
                timeoutSettings.setDebugExecutionTimeout(timeout); else
                timeoutSettings.setExecutionTimeout(timeout);

            executionTimeoutTextField.requestFocus();
        }

        @Override
        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(!hasErrors);
        }
    }

    class ReloadDefaultAction extends BasicAction {

        ReloadDefaultAction() {
            super(txt("app.execution.action.ReloadFromSettings"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            int timeout = getSettingsTimeout();
            executionTimeoutTextField.setText(String.valueOf(timeout));
            executionTimeoutTextField.requestFocus();
        }

        @Override
        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(!hasErrors);
        }
    }

}
