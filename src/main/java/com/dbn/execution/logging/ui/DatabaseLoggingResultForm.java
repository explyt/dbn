package com.dbn.execution.logging.ui;

import com.dbn.common.action.DataKeys;
import com.dbn.common.dispose.Disposer;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Actions;
import com.dbn.connection.ConnectionHandler;
import com.dbn.execution.common.result.ui.ExecutionResultFormBase;
import com.dbn.execution.logging.DatabaseLoggingResult;
import com.intellij.ide.actions.NextOccurenceToolbarAction;
import com.intellij.ide.actions.PreviousOccurenceToolbarAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DatabaseLoggingResultForm extends ExecutionResultFormBase<DatabaseLoggingResult> {
    private JPanel mainPanel;
    private JPanel consolePanel;
    private JPanel actionsPanel;

    private final DatabaseLoggingResultConsole console;

    public DatabaseLoggingResultForm(@NotNull DatabaseLoggingResult loggingResult) {
        super(loggingResult);
        ConnectionHandler connection = loggingResult.getConnection();
        console = new DatabaseLoggingResultConsole(connection, loggingResult.getName(), false);

        JComponent consoleComponent = console.getComponent();
        consoleComponent.setBorder(Borders.lineBorder(JBColor.border(), 0, 1, 1, 0));
        consolePanel.add(consoleComponent, BorderLayout.CENTER);

        ActionManager actionManager = ActionManager.getInstance();
        //ActionGroup actionGroup = (ActionGroup) actionManager.getAction("DBNavigator.ActionGroup.DatabaseLogOutput");
        DefaultActionGroup toolbarActions = (DefaultActionGroup) console.getToolbarActions();
        if (toolbarActions != null) {
            for (AnAction action : toolbarActions.getChildActionsOrStubs()) {
                if (action instanceof PreviousOccurenceToolbarAction || action instanceof NextOccurenceToolbarAction) {
                    toolbarActions.remove(action);
                }
            }

            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.KillProcess"), Constraints.FIRST);
            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.RerunProcess"), Constraints.FIRST);
            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.Close"), Constraints.FIRST);
            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.Settings"), Constraints.LAST);
            ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel, "", false, toolbarActions);
            actionsPanel.add(actionToolbar.getComponent());
            actionToolbar.setTargetComponent(console.getToolbarContextComponent());
        }

        Disposer.register(this, console);
    }

    public DatabaseLoggingResultConsole getConsole() {
        return console;
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    @Override
    public @Nullable Object getData(@NotNull String dataId) {
        if (DataKeys.DATABASE_LOG_OUTPUT.is(dataId)) return getExecutionResult();
        return null;
    }
}
