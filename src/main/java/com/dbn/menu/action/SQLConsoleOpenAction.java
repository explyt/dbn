package com.dbn.menu.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.util.Actions;
import com.dbn.common.util.Editors;
import com.dbn.connection.ConnectionBundle;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionManager;
import com.dbn.connection.ConnectionRef;
import com.dbn.connection.console.DatabaseConsoleManager;
import com.dbn.database.DatabaseFeature;
import com.dbn.object.DBConsole;
import com.dbn.vfs.DBConsoleType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SQLConsoleOpenAction extends ProjectAction {

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Open SQL Console...");
        presentation.setIcon(Icons.SQL_CONSOLE);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        //FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.file");
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        List<ConnectionHandler> connections = connectionBundle.getConnections();
        if (connections.isEmpty()) {
            connectionManager.promptMissingConnection();
            return;
        }

        if (connections.size() == 1) {
            openSQLConsole(connections.get(0));
            return;
        }

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.addSeparator();
        for (ConnectionHandler connection : connections) {
            actionGroup.add(new SelectConnectionAction(connection));
        }

        ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                "Select Console Connection",
                actionGroup,
                e.getDataContext(),
                //JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                false,
                true,
                true,
                null,
                actionGroup.getChildrenCount(),
                preselect -> {
/*
                        SelectConsoleAction selectConnectionAction = (SelectConsoleAction) action;
                        return latestSelection == selectConnectionAction.connection;
*/
                    return true;
                });

        popupBuilder.showCenteredInCurrentWindow(project);

    }

    private static class SelectConnectionAction extends ActionGroup {
        private final ConnectionRef connection;

        private SelectConnectionAction(ConnectionHandler connection) {
            super(connection.getName(), null, connection.getIcon());
            this.connection = ConnectionRef.of(connection);
            setPopup(true);
        }
/*
        @Override
        public void actionPerformed(AnActionEvent e) {
            openSQLConsole(connection);
            latestSelection = connection;
        }*/

        @NotNull
        @Override
        public AnAction[] getChildren(AnActionEvent e) {
            ConnectionHandler connection = this.connection.ensure();
            List<AnAction> actions = new ArrayList<>();
            Collection<DBConsole> consoles = connection.getConsoleBundle().getConsoles();
            for (DBConsole console : consoles) {
                actions.add(new SelectConsoleAction(console));
            }
            actions.add(Separator.getInstance());
            actions.add(new SelectConsoleAction(connection, DBConsoleType.STANDARD));
            if (DatabaseFeature.DEBUGGING.isSupported(connection)) {
                actions.add(new SelectConsoleAction(connection, DBConsoleType.DEBUG));
            }

            return actions.toArray(new AnAction[0]);
        }
    }

    private static class SelectConsoleAction extends BasicAction {
        private ConnectionRef connection;
        private DBConsole console;
        private DBConsoleType consoleType;


        SelectConsoleAction(ConnectionHandler connection, DBConsoleType consoleType) {
            super("New " + consoleType.getName() + "...");
            this.connection = ConnectionRef.of(connection);
            this.consoleType = consoleType;
        }

        SelectConsoleAction(DBConsole console) {
            super(Actions.adjustActionName(console.getName()), null, console.getIcon());
            this.console = console;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (console == null) {
                ConnectionHandler connection = this.connection.ensure();
                DatabaseConsoleManager consoleManager = DatabaseConsoleManager.getInstance(connection.getProject());
                consoleManager.showCreateConsoleDialog(connection, consoleType);
            } else {
                ConnectionHandler connection = console.ensureConnection();
                Editors.openFileEditor(connection.getProject(), console.getVirtualFile(), true);
            }
        }
    }

    private static void openSQLConsole(ConnectionHandler connection) {
        DBConsole defaultConsole = connection.getConsoleBundle().getDefaultConsole();
        Editors.openFileEditor(connection.getProject(), defaultConsole.getVirtualFile(), true);
    }
}
