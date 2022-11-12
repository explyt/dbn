package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.dci.intellij.dbn.common.message.MessageCallback.when;

public class SQLConsoleOpenAction extends DumbAwareProjectAction {
    public SQLConsoleOpenAction() {
        super("Open SQL console...", null, Icons.FILE_SQL_CONSOLE);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        //FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.file");
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();


        ConnectionHandler singleConnectionHandler = null;
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (connectionBundle.getConnections().size() > 0) {
            actionGroup.addSeparator();
            for (ConnectionHandler connection : connectionBundle.getConnections()) {
                SelectConnectionAction connectionAction = new SelectConnectionAction(connection);
                actionGroup.add(connectionAction);
                singleConnectionHandler = connection;
            }
        }

        if (actionGroup.getChildrenCount() > 1) {
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
        } else {
            if (singleConnectionHandler != null) {
                openSQLConsole(singleConnectionHandler);
            } else {
                Messages.showInfoDialog(
                        project, "No connections available.", "No database connections found. Please setup a connection first",
                        new String[]{"Setup Connection", "Cancel"}, 0,
                        option -> when(option == 0, () -> {
                            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
                            settingsManager.openProjectSettings(ConfigId.CONNECTIONS);
                        }));
            }

        }

    }

    private class SelectConnectionAction extends ActionGroup {
        private ConnectionRef connection;

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

    private static class SelectConsoleAction extends AnAction{
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
                ConnectionHandler connection = Failsafe.nn(console.getConnection());
                FileEditorManager editorManager = FileEditorManager.getInstance(connection.getProject());
                editorManager.openFile(console.getVirtualFile(), true);
            }
        }
    }

    private static void openSQLConsole(ConnectionHandler connection) {
        FileEditorManager editorManager = FileEditorManager.getInstance(connection.getProject());
        DBConsole defaultConsole = connection.getConsoleBundle().getDefaultConsole();
        editorManager.openFile(defaultConsole.getVirtualFile(), true);
    }
}