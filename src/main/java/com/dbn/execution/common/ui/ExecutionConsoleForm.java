package com.dbn.execution.common.ui;

import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.environment.EnvironmentType;
import com.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.icon.Icons;
import com.dbn.common.message.MessageType;
import com.dbn.common.navigation.NavigationInstructions;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.tab.TabbedPane;
import com.dbn.common.ui.util.Mouse;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.common.util.Documents;
import com.dbn.common.util.Strings;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.execution.ExecutionManager;
import com.dbn.execution.ExecutionResult;
import com.dbn.execution.common.message.ui.ExecutionMessagesForm;
import com.dbn.execution.common.options.ExecutionEngineSettings;
import com.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dbn.execution.compiler.CompilerMessage;
import com.dbn.execution.compiler.CompilerResult;
import com.dbn.execution.explain.result.ExplainPlanMessage;
import com.dbn.execution.explain.result.ExplainPlanResult;
import com.dbn.execution.logging.DatabaseLoggingResult;
import com.dbn.execution.logging.LogOutput;
import com.dbn.execution.logging.LogOutputContext;
import com.dbn.execution.method.result.MethodExecutionResult;
import com.dbn.execution.statement.StatementExecutionInput;
import com.dbn.execution.statement.StatementExecutionMessage;
import com.dbn.execution.statement.options.StatementExecutionSettings;
import com.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dbn.execution.statement.result.StatementExecutionResult;
import com.dbn.language.common.DBLanguagePsiFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentTransactionListener;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.TabLabel;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.*;

import static com.dbn.common.dispose.Checks.isNotValid;
import static com.dbn.common.dispose.Checks.isValid;
import static com.dbn.common.dispose.Failsafe.guarded;
import static com.dbn.common.navigation.NavigationInstruction.*;
import static com.dbn.common.util.Unsafe.cast;

public class ExecutionConsoleForm extends DBNFormBase {
    private JPanel mainPanel;
    private TabbedPane resultTabs;
    private ExecutionMessagesForm executionMessagesForm;
    private final Map<ExecutionResult, ExecutionResultForm> executionResultForms = ContainerUtil.createConcurrentWeakKeySoftValueMap();

    private boolean canScrollToSource;

    public ExecutionConsoleForm(Disposable parent, Project project) {
        super(parent, project);
        ProjectEvents.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener());
        ProjectEvents.subscribe(project, this, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener());
    }

    @NotNull
    private EnvironmentManagerListener environmentManagerListener() {
        return new EnvironmentManagerListener() {
            @Override
            public void configurationChanged(Project project) {
                EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
                TabbedPane resultTabs = getResultTabs();
                for (TabInfo tabInfo : resultTabs.getTabs()) {
                    updateTab(visibilitySettings, tabInfo);
                }
            }

            private void updateTab(EnvironmentVisibilitySettings visibilitySettings, TabInfo tabInfo) {
                ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
                if (executionResult != null) {
                    ConnectionHandler connection = executionResult.getConnection();
                    EnvironmentType environmentType = connection.getEnvironmentType();
                    if (visibilitySettings.getExecutionResultTabs().value()) {
                        tabInfo.setTabColor(environmentType.getColor());
                    } else {
                        tabInfo.setTabColor(null);
                    }
                }
            }
        };
    }

    @NotNull
    private PsiDocumentTransactionListener psiDocumentTransactionListener() {
        return new PsiDocumentTransactionListener() {

            @Override
            public void transactionStarted(@NotNull Document document, @NotNull PsiFile file) {

            }

            @Override
            public void transactionCompleted(@NotNull Document document, @NotNull PsiFile file) {
                guarded(() -> refreshResultTabs(file));
            }
        };
    }

    private void refreshResultTabs(@NotNull PsiFile file) {
        TabbedPane resultTabs = getResultTabs();
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
            if (executionResult instanceof StatementExecutionResult) {
                StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                StatementExecutionProcessor executionProcessor = statementExecutionResult.getExecutionProcessor();
                if (isValid(executionProcessor) && Objects.equals(file, executionProcessor.getPsiFile())) {
                    Icon icon = executionProcessor.isDirty() ?
                            Icons.STMT_EXEC_RESULTSET_ORPHAN :
                            Icons.STMT_EXEC_RESULTSET;
                    tabInfo.setIcon(icon);
                }
            }
        }
        ExecutionMessagesForm messagesPanel = executionMessagesForm;
        if (messagesPanel != null) {
            JComponent messagePanelComponent = messagesPanel.getComponent();
            UserInterface.repaint(messagePanelComponent);
        }
    }


    private TabbedPane getResultTabs() {
        if (!isValid(resultTabs) && !isDisposed()) {
            resultTabs = new TabbedPane(this);
            mainPanel.removeAll();
            mainPanel.add(resultTabs, BorderLayout.CENTER);
            resultTabs.setFocusable(false);
            //resultTabs.setAdjustBorders(false);
            resultTabs.addTabMouseListener(mouseListener);
            resultTabs.addListener(tabsListener);
            resultTabs.setPopupGroup(new ExecutionConsolePopupActionGroup(this), "place", false);
            resultTabs.setTabsPosition(JBTabsPosition.bottom);
            resultTabs.setBorder(null);
            Disposer.register(this, resultTabs);
            return resultTabs;
        }
        return Failsafe.nn(resultTabs);
    }

    private int getTabCount() {
        return getResultTabs().getTabCount();
    }

    private final MouseListener mouseListener = Mouse.listener().onClick(e -> {
        if (e.isShiftDown() && (16 & e.getModifiers()) > 0 || ((8 & e.getModifiers()) > 0)) {
            if (e.getSource() instanceof TabLabel) {
                TabLabel tabLabel = (TabLabel) e.getSource();
                removeTab(tabLabel.getInfo());
            }
        }
    });

    private final TabsListener tabsListener = new TabsListener() {
        @Override
        public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
            if (canScrollToSource) {
                if (newSelection != null) {
                    ExecutionResult<?> executionResult = getExecutionResult(newSelection);
                    if (isNotValid(executionResult)) return;
                    if (executionResult instanceof StatementExecutionResult) {
                        StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                        statementExecutionResult.navigateToEditor(NavigationInstructions.create(FOCUS, SCROLL));
                    }
                }
            }
            if (oldSelection != null && newSelection != null && getExecutionResult(newSelection) instanceof DatabaseLoggingResult) {
                newSelection.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE);
            }

        }
    };

    public void removeAllExceptTab(TabInfo exceptionTabInfo) {
        for (TabInfo tabInfo : getResultTabs().getTabs()) {
            if (tabInfo != exceptionTabInfo) {
                removeTab(tabInfo);
            }
        }
    }

    public synchronized void removeTab(TabInfo tabInfo) {
        ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
        if (executionResult == null) {
            removeMessagesTab();
        } else {
            removeResultTab(executionResult);
        }
    }

    public void removeAllTabs() {
        for (TabInfo tabInfo : getResultTabs().getTabs()) {
            removeTab(tabInfo);
        }
    }

    @NotNull
    @Override
    public JComponent getMainComponent() {
        return getResultTabs();
    }

    public void selectResult(StatementExecutionResult executionResult, NavigationInstructions instructions) {
        StatementExecutionMessage executionMessage = executionResult.getExecutionMessage();
        if (executionMessage != null) {
            prepareMessagesTab(instructions);
            ExecutionMessagesForm messagesPane = ensureExecutionMessagesPanel();
            messagesPane.selectMessage(executionMessage, instructions);
        }
    }

    public void addResult(ExplainPlanResult explainPlanResult, NavigationInstructions instructions) {
        if (explainPlanResult.isError()) {
            prepareMessagesTab(instructions.with(RESET));
            ExecutionMessagesForm messagesPane = ensureExecutionMessagesPanel();
            ExplainPlanMessage explainPlanMessage = new ExplainPlanMessage(explainPlanResult, MessageType.ERROR);
            messagesPane.addExplainPlanMessage(explainPlanMessage, instructions);
        } else {
            showResultTab(explainPlanResult);
        }
    }

    public void addResult(StatementExecutionResult executionResult, NavigationInstructions instructions) {
        ExecutionMessagesForm messagesPane = ensureExecutionMessagesPanel();
        TreePath messageTreePath = null;
        CompilerResult compilerResult = executionResult.getCompilerResult();
        //boolean hasCompilerResult = compilerResult != null;
        //boolean selectMessage = !executionResult.getExecutionProcessor().getExecutionInput().isBulkExecution() && !hasCompilerResult;
        //boolean focusMessage = selectMessage && focusOnExecution();
        StatementExecutionMessage executionMessage = executionResult.getExecutionMessage();
        if (executionResult instanceof StatementExecutionCursorResult) {
            if (executionMessage == null) {
                showResultTab(executionResult);
            } else {
                prepareMessagesTab(instructions.with(RESET));
                messageTreePath = messagesPane.addExecutionMessage(executionMessage, instructions);
            }
        } else if (executionMessage != null) {
            prepareMessagesTab(instructions.with(RESET));
            messageTreePath = messagesPane.addExecutionMessage(executionMessage, instructions);
        }

        if (compilerResult != null) {
            addCompilerResult(compilerResult);
        }
        if (messageTreePath != null) {
            messagesPane.expand(messageTreePath);
        }
    }

    private boolean focusOnExecution() {
        Project project = ensureProject();
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
        StatementExecutionSettings statementExecutionSettings = executionEngineSettings.getStatementExecutionSettings();
        return statementExecutionSettings.isFocusResult();
    }

    public void addCompilerResult(@NotNull CompilerResult compilerResult) {
        boolean bulk = compilerResult.getCompilerAction().isBulkCompile();
        boolean error = compilerResult.isError();
        boolean single = compilerResult.isSingleMessage();

        prepareMessagesTab(NavigationInstructions.create(RESET));

        CompilerMessage firstMessage = null;
        ExecutionMessagesForm messagesPanel = ensureExecutionMessagesPanel();

        for (CompilerMessage compilerMessage : compilerResult.getCompilerMessages()) {
            if (firstMessage == null) {
                firstMessage = compilerMessage;
            }
            messagesPanel.addCompilerMessage(compilerMessage,
                    NavigationInstructions.create().
                    with(FOCUS, !bulk && error && single).
                    with(SCROLL, single));
        }

        if (firstMessage != null && firstMessage.isError() && !bulk) {
            messagesPanel.selectMessage(firstMessage, NavigationInstructions.create(SCROLL, SELECT, OPEN));
        }
    }

    public void addResult(MethodExecutionResult executionResult) {
        showResultTab(executionResult);
    }

    public ExecutionResult<?> getSelectedExecutionResult() {
        TabInfo selectedInfo = getResultTabs().getSelectedInfo();
        return selectedInfo == null ? null : getExecutionResult(selectedInfo);
    }

    @Nullable
    private static ExecutionResult<?> getExecutionResult(TabInfo tabInfo) {
        ExecutionResultForm<?> executionResultForm = (ExecutionResultForm<?>) tabInfo.getObject();
        return isValid(executionResultForm) ? executionResultForm.getExecutionResult() : null;
    }

    /*********************************************************
     *                       Messages                        *
     *********************************************************/
    private ExecutionMessagesForm ensureExecutionMessagesPanel() {
        if (executionMessagesForm == null) {
            executionMessagesForm = new ExecutionMessagesForm(this);
        }
        return executionMessagesForm;
    }

    private void prepareMessagesTab(NavigationInstructions instructions) {
        TabbedPane resultTabs = getResultTabs();
        ExecutionMessagesForm messagesPanel = ensureExecutionMessagesPanel();
        if (instructions.isReset()) {
            messagesPanel.resetMessagesStatus();
        }
        JComponent component = messagesPanel.getComponent();
        if (resultTabs.getTabCount() == 0 || resultTabs.getTabAt(0).getComponent() != component) {
            TabInfo tabInfo = new TabInfo(component);

            tabInfo.setText("Messages");
            tabInfo.setIcon(Icons.EXEC_RESULT_MESSAGES);
            resultTabs.addTab(tabInfo, 0);
        }

        TabInfo tabInfo = resultTabs.getTabAt(0);
        resultTabs.select(tabInfo, instructions.isFocus());
    }


    public void removeMessagesTab() {
        ExecutionMessagesForm messagesPanel = this.executionMessagesForm;
        if (messagesPanel == null) return;

        TabbedPane resultTabs = getResultTabs();
        JComponent component = messagesPanel.getComponent();
        if (resultTabs.getTabCount() > 0 || resultTabs.getTabAt(0).getComponent() == component) {
            TabInfo tabInfo = resultTabs.getTabAt(0);
            resultTabs.removeTab(tabInfo);
        }

        messagesPanel.reset();
        Disposer.dispose(messagesPanel);
        this.executionMessagesForm = null;
        if (getTabCount() == 0) {
            getExecutionManager().hideExecutionConsole();
        }
    }

    @NotNull
    public ExecutionManager getExecutionManager() {
        Project project = ensureProject();
        return ExecutionManager.getInstance(project);
    }

    private boolean isMessagesTabVisible() {
        TabbedPane resultTabs = getResultTabs();
        if (resultTabs.getTabCount() > 0) {
            JComponent messagesPanelComponent = ensureExecutionMessagesPanel().getComponent();
            TabInfo tabInfo = resultTabs.getTabAt(0);
            return tabInfo.getComponent() == messagesPanelComponent;
        }
        return false;
    }

    /*********************************************************
     *                       Logging                         *
     *********************************************************/
    public void displayLogOutput(LogOutputContext context, LogOutput output) {
        TabbedPane resultTabs = getResultTabs();
        boolean emptyOutput = Strings.isEmptyOrSpaces(output.getText());
        VirtualFile sourceFile = context.getSourceFile();
        ConnectionHandler connection = context.getConnection();
        boolean selectTab = sourceFile != null;
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
            if (executionResult instanceof DatabaseLoggingResult) {
                DatabaseLoggingResult logOutput = (DatabaseLoggingResult) executionResult;
                if (logOutput.matches(context)) {
                    logOutput.write(context, output);
                    if (!emptyOutput && !selectTab) {
                        tabInfo.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE_UNREAD);
                    }
                    if (selectTab) {
                        resultTabs.select(tabInfo, true);
                    }
                    return;
                }
            }
        }
        boolean messagesTabVisible = isMessagesTabVisible();

        DatabaseLoggingResult logOutput = new DatabaseLoggingResult(context);
        ExecutionResultForm<?> resultForm = ensureResultForm(logOutput);
        if (isValid(resultForm)) {
            JComponent component = resultForm.getComponent();
            TabInfo tabInfo = new TabInfo(component);
            tabInfo.setObject(resultForm);
            tabInfo.setText(logOutput.getName());
            tabInfo.setIcon(emptyOutput || selectTab ?
                    Icons.EXEC_LOG_OUTPUT_CONSOLE :
                    Icons.EXEC_LOG_OUTPUT_CONSOLE_UNREAD);
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
            if (visibilitySettings.getExecutionResultTabs().value()){
                tabInfo.setTabColor(connection.getEnvironmentType().getColor());
            } else {
                tabInfo.setTabColor(null);
            }

            resultTabs.addTab(tabInfo, messagesTabVisible ? 1 : 0);
            if (selectTab) {
                resultTabs.select(tabInfo, true);
            }
            logOutput.write(context, output);
        }
    }

    /*********************************************************
     *           Statement / method executions               *
     *********************************************************/
    private void showResultTab(ExecutionResult<?> executionResult) {
        if (executionResult instanceof ExplainPlanResult) {
            addResultTab(executionResult);
        } else {
            ExecutionResult<?> previousExecutionResult = executionResult.getPrevious();

            ExecutionResultForm executionResultForm;
            if (previousExecutionResult == null) {
                executionResultForm = getExecutionResultForm(executionResult);
                if (executionResultForm != null) {
                    selectResultTab(executionResult);
                }
            } else {
                executionResultForm = getExecutionResultForm(previousExecutionResult);
                if (executionResultForm != null) {
                    executionResultForms.remove(previousExecutionResult);
                    executionResultForms.put(executionResult, executionResultForm);
                    executionResultForm.setExecutionResult(executionResult);
                    selectResultTab(executionResult);
                }
            }

            if (executionResultForm == null) {
                addResultTab(executionResult);
            }
        }
    }

    private void addResultTab(ExecutionResult<?> executionResult) {
        ExecutionResultForm<?> resultForm = ensureResultForm(executionResult);
        if (isNotValid(resultForm)) return;

        JComponent component = resultForm.getComponent();
        TabInfo tabInfo = new TabInfo(component);
        tabInfo.setObject(resultForm);
        EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
        if (visibilitySettings.getExecutionResultTabs().value()){
            tabInfo.setTabColor(executionResult.getConnection().getEnvironmentType().getColor());
        } else {
            tabInfo.setTabColor(null);
        }
        tabInfo.setText(executionResult.getName());
        tabInfo.setIcon(executionResult.getIcon());
        getResultTabs().addTab(tabInfo);
        selectResultTab(tabInfo);
    }

    public void removeResultTab(ExecutionResult<?> executionResult) {
        try {
            canScrollToSource = false;
            TabbedPane resultTabs = getResultTabs();
            ExecutionResultForm<?> resultForm = executionResultForms.remove(executionResult);
            if (resultForm == null) return;

            try {
                TabInfo tabInfo = resultTabs.findInfo(resultForm.getComponent());
                if (resultTabs.getTabs().contains(tabInfo)) {
                    DBLanguagePsiFile file = null;
                    if (executionResult instanceof StatementExecutionResult) {
                        StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                        StatementExecutionInput executionInput = statementExecutionResult.getExecutionInput();
                        file = executionInput.getExecutionProcessor().getPsiFile();
                    }

                    resultTabs.removeTab(tabInfo);
                    Documents.refreshEditorAnnotations(file);
                }
            } finally {
                if (getTabCount() == 0) {
                    getExecutionManager().hideExecutionConsole();
                }
            }
        } finally {
            canScrollToSource = true;
        }
    }

    public <T extends ExecutionResult<?>> void selectResultTab(T executionResult) {
        ExecutionResultForm<?> resultForm = getExecutionResultForm(executionResult);
        if (isNotValid(resultForm)) return;


        JComponent component = resultForm.getComponent();
        TabInfo tabInfo = getResultTabs().findInfo(component);
        if (tabInfo == null) return;


        tabInfo.setText(executionResult.getName());
        tabInfo.setIcon(executionResult.getIcon());
        selectResultTab(tabInfo);
    }

    public void closeExecutionResults(List<ConnectionId> connectionIds) {
        List<TabInfo> tabs = getExecutionResultTabs();
        for (TabInfo tabInfo : tabs) {
            ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
            if (executionResult != null && connectionIds.contains(executionResult.getConnectionId())) {
                removeTab(tabInfo);
            }
        }
    }

    @NotNull
    private List<TabInfo> getExecutionResultTabs() {
        return isValid(resultTabs) ? new ArrayList<>(resultTabs.getTabs()) : Collections.emptyList();
    }

    @Nullable
    private ExecutionResultForm<?> ensureResultForm(ExecutionResult<?> executionResult) {
        return executionResultForms.computeIfAbsent(executionResult, k -> executionResult.createForm());
    }


    /*********************************************************
     *                      Miscellaneous                    *
     *********************************************************/

    private boolean containsResultTab(Component component) {
        TabbedPane resultTabs = getResultTabs();
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            if (tabInfo.getComponent() == component) {
                return true;
            }
        }
        return false;
    }

    private void selectResultTab(TabInfo tabInfo) {
        TabbedPane resultTabs = getResultTabs();
        try {
            canScrollToSource = false;
            resultTabs.select(tabInfo, true);
        } finally {
            canScrollToSource = true;
        }
    }

    @Nullable
    public <T extends ExecutionResultForm> T getExecutionResultForm(ExecutionResult<?> executionResult) {
        return cast(executionResultForms.get(executionResult));
    }
}
