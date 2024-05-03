package com.dbn.execution.script.options.ui;

import com.dbn.common.action.BasicAction;
import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dbn.connection.DatabaseType;
import com.dbn.execution.script.CmdLineInterfaceBundle;
import com.dbn.execution.script.ScriptExecutionManager;
import com.dbn.execution.script.options.ScriptExecutionSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static com.dbn.common.ui.util.UserInterface.createToolbarDecorator;

public class ScriptExecutionSettingsForm extends ConfigurationEditorForm<ScriptExecutionSettings> {
    private JPanel mainPanel;
    private JPanel cmdLineInterfacesTablePanel;
    private JTextField executionTimeoutTextField;
    private final CmdLineInterfacesTable cmdLineInterfacesTable;

    public ScriptExecutionSettingsForm(ScriptExecutionSettings settings) {
        super(settings);
        cmdLineInterfacesTable = new CmdLineInterfacesTable(this, settings.getCommandLineInterfaces());

        ToolbarDecorator decorator = createToolbarDecorator(cmdLineInterfacesTable);
        decorator.setAddAction(anActionButton ->
                showNewInterfacePopup(
                        anActionButton.getDataContext(),
                        anActionButton.getPreferredPopupPoint()));
        decorator.setRemoveAction(anActionButton -> cmdLineInterfacesTable.removeRow());
        decorator.setMoveUpAction(anActionButton -> cmdLineInterfacesTable.moveRowUp());
        decorator.setMoveDownAction(anActionButton -> cmdLineInterfacesTable.moveRowDown());
        decorator.setPreferredSize(new Dimension(-1, 300));
        JPanel panel = decorator.createPanel();
        cmdLineInterfacesTablePanel.add(panel, BorderLayout.CENTER);
        cmdLineInterfacesTable.getParent().setBackground(cmdLineInterfacesTable.getBackground());
        executionTimeoutTextField.setText(String.valueOf(settings.getExecutionTimeout()));
        registerComponents(mainPanel);
    }

    private void showNewInterfacePopup(DataContext dataContext, RelativePoint point) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (DatabaseType databaseType : DatabaseType.values()) {
            if (databaseType != DatabaseType.GENERIC){
                actionGroup.add(new CreateInterfaceAction(databaseType));
            }
        }

        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                null,
                actionGroup,
                dataContext,
                null,
                false);

        popup.show(point);
    }

    public class CreateInterfaceAction extends BasicAction {
        private final DatabaseType databaseType;
        CreateInterfaceAction(DatabaseType databaseType) {
            super();
            getTemplatePresentation().setText(databaseType.getName(), false);
            getTemplatePresentation().setIcon(databaseType.getIcon());
            this.databaseType = databaseType;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project != null) {
                ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
                scriptExecutionManager.createCmdLineInterface(
                        databaseType,
                        cmdLineInterfacesTable.getNames(),
                        inputValue -> cmdLineInterfacesTable.addInterface(inputValue));
            }
        }
    }
    
    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
    
    @Override
    public void applyFormChanges() throws ConfigurationException {
        ScriptExecutionSettings configuration = getConfiguration();
        int executionTimeout = ConfigurationEditorUtil.validateIntegerValue(executionTimeoutTextField, "Execution timeout", true, 0, 6000, "\nUse value 0 for no timeout");
        CmdLineInterfacesTableModel model = cmdLineInterfacesTable.getModel();
        model.validate();
        CmdLineInterfaceBundle executorBundle = model.getBundle();
        configuration.setCommandLineInterfaces(executorBundle);
        configuration.setExecutionTimeout(executionTimeout);
    }

    @Override
    public void resetFormChanges() {
        ScriptExecutionSettings settings = getConfiguration();
        executionTimeoutTextField.setText(Integer.toString(settings.getExecutionTimeout()));
        cmdLineInterfacesTable.getModel().setBundle(settings.getCommandLineInterfaces());
    }
}
