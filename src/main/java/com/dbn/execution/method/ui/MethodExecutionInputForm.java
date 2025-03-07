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

package com.dbn.execution.method.ui;

import com.dbn.common.dispose.DisposableContainers;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.dialog.DBNDialog;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.form.DBNHeaderForm;
import com.dbn.common.ui.misc.DBNScrollPane;
import com.dbn.common.ui.panel.DBNCollapsiblePanel;
import com.dbn.common.ui.util.Accessibility;
import com.dbn.common.ui.util.Listeners;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.debugger.DBDebuggerType;
import com.dbn.execution.common.ui.ExecutionOptionsForm;
import com.dbn.execution.method.MethodExecutionInput;
import com.dbn.object.DBArgument;
import com.dbn.object.DBMethod;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.AsyncProcessIcon;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

public class MethodExecutionInputForm extends DBNFormBase {
    private JPanel mainPanel;
    private JPanel argumentsPanel;
    private JPanel headerPanel;
    private JLabel noArgumentsLabel;
    private JLabel debuggerVersionLabel;
    private JPanel versionPanel;
    private JLabel debuggerTypeLabel;
    private JPanel executionOptionsPanel;
    private JPanel argumentsContainerPanel;
    private JPanel loadingArgumentsPanel;
    private JPanel loadingArgumentsIconPanel;
    private DBNScrollPane argumentsScrollPane;

    private final List<MethodExecutionInputArgumentForm> argumentForms = DisposableContainers.list(this);
    private final ExecutionOptionsForm executionOptionsForm;
    private final Listeners<ChangeListener> changeListeners = Listeners.create(this);

    @Getter @Setter
    private MethodExecutionInput executionInput;

    public MethodExecutionInputForm(
            DBNComponent parentComponent,
            @NotNull MethodExecutionInput executionInput,
            boolean showHeader,
            @NotNull DBDebuggerType debuggerType) {

        super(parentComponent);
        this.executionInput = executionInput;
        DBObjectRef<?> methodRef = executionInput.getMethodRef();

        if (debuggerType.isDebug()) {
            versionPanel.setVisible(true);
            debuggerTypeLabel.setText(debuggerType.name());
            debuggerVersionLabel.setText("...");
            Dispatch.async(
                    debuggerVersionLabel,
                    () -> executionInput.getDebuggerVersion(),
                    v -> debuggerVersionLabel.setText(v));
        } else {
            versionPanel.setVisible(false);
        }


        executionOptionsForm = new ExecutionOptionsForm(this, executionInput, debuggerType);

        DBNCollapsiblePanel collapsiblePanel = new DBNCollapsiblePanel(this, executionOptionsForm, false);
        collapsiblePanel.setExpanded(executionInput.isContextExpanded());
        collapsiblePanel.addToggleListener(expanded -> executionInput.setContextExpanded(expanded));
        executionOptionsPanel.add(collapsiblePanel.getComponent());

        if (showHeader) {
            DBNHeaderForm headerForm = new DBNHeaderForm(this, methodRef);
            headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        }
        headerPanel.setVisible(showHeader);


        initArgumentsPanel();
    }

    private boolean methodDetailsInitialized() {
        // method details are expected to be pre-initialized if opened in an isolated execution dialog
        // TODO find a clearer solution to this
        return getParentComponent() instanceof DBNDialog;
    }

    private void initArgumentsPanel() {
        if (methodDetailsInitialized()) {
            createArgumentsPanel();
            return;
        }
        noArgumentsLabel.setVisible(false);
        loadingArgumentsPanel.setVisible(true);
        loadingArgumentsIconPanel.add(new AsyncProcessIcon("Loading"), BorderLayout.CENTER);

        //lazy arguments initialization
        Dispatch.async(
                mainPanel,
                () -> getExecutionInput().initDatabaseElements(),
                () -> createArgumentsPanel());
    }

    private void createArgumentsPanel() {
        List<DBArgument> arguments = getMethodArguments();
        checkDisposed();

        loadingArgumentsPanel.setVisible(false);
        loadingArgumentsIconPanel.removeAll();
        argumentsPanel.setLayout(new BoxLayout(argumentsPanel, BoxLayout.Y_AXIS));
        int[] metrics = new int[]{0, 0, 0};

        boolean noArguments = true;
        for (DBArgument argument: arguments) {
            if (argument.isInput()) {
                metrics = addArgumentPanel(argument, metrics);
                noArguments = false;
            }
        }
        noArgumentsLabel.setVisible(noArguments);

        for (MethodExecutionInputArgumentForm component : argumentForms) {
            component.adjustMetrics(metrics);
        }

        if (argumentForms.isEmpty()) {
            argumentsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            Dimension preferredSize = argumentsScrollPane.getViewport().getView().getPreferredSize();
            preferredSize.setSize(preferredSize.getWidth(), preferredSize.getHeight() + 2);
            argumentsScrollPane.setMinimumSize(preferredSize);
        } else {
            MethodExecutionInputArgumentForm firstArgumentForm = argumentForms.get(0);
            int scrollUnitIncrement = firstArgumentForm.getScrollUnitIncrement();
            Dimension minSize = new Dimension(-1, Math.min(argumentForms.size(), 10) * scrollUnitIncrement + 2);
            argumentsScrollPane.setMinimumSize(minSize);
            argumentsScrollPane.getVerticalScrollBar().setUnitIncrement(scrollUnitIncrement);
            Accessibility.setAccessibleName(argumentsScrollPane, "Method arguments");
        }

        for (MethodExecutionInputArgumentForm argumentComponent : argumentForms){
            argumentComponent.addDocumentListener(documentListener);
        }
        updatePreferredSize();
    }

    private void updatePreferredSize() {
        Dimension preferredSize = mainPanel.getPreferredSize();
        int width = (int) preferredSize.getWidth() + 24;
        int height = (int) Math.min(preferredSize.getHeight(), 380);
        mainPanel.setPreferredSize(new Dimension(width, height));
        UserInterface.repaint(mainPanel);
    }

    private List<DBArgument> getMethodArguments() {
        DBMethod method = executionInput.getMethod();
        return method == null ? Collections.emptyList() : method.getArguments();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    private int[] addArgumentPanel(DBArgument argument, int[] gridMetrics) {
        MethodExecutionInputArgumentForm argumentComponent = new MethodExecutionInputArgumentForm(this, argument);
        argumentsPanel.add(argumentComponent.getComponent());
        argumentForms.add(argumentComponent);
        return argumentComponent.getMetrics(gridMetrics);
   }

    public void updateExecutionInput() {
        for (MethodExecutionInputArgumentForm argumentComponent : argumentForms) {
            argumentComponent.updateExecutionInput();
        }
        executionOptionsForm.updateExecutionInput();
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
        executionOptionsForm.addChangeListener(changeListener);
    }

    private final DocumentListener documentListener = new DocumentAdapter() {
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
            notifyChangeListeners();
        }
    };

    private void notifyChangeListeners() {
        ChangeEvent changeEvent = new ChangeEvent(this);
        changeListeners.notify(l -> l.stateChanged(changeEvent));
    }

    @Deprecated
    public void touch() {
        executionOptionsForm.touch();
    }
}
