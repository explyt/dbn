package com.dbn.oracleAI.config.AIProviders.ui;


import com.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.icon.Icons;
import com.dbn.common.options.SettingsChangeNotifier;
import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.oracleAI.config.AIProviders.AIProviderTypeBundle;
import com.dbn.oracleAI.config.AIProviders.AIProvidersGeneralSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class AIProvidersGeneralSettingsForm extends ConfigurationEditorForm<AIProvidersGeneralSettings> {
  private JPanel mainPanel;
  private JPanel environmentTypesTablePanel;

  private AIProviderEditorTable environmentTypesTable;

  public AIProvidersGeneralSettingsForm(AIProvidersGeneralSettings settings) {
    super(settings);

    environmentTypesTable = new AIProviderEditorTable(this, settings.getAIProviderTypes());


    ToolbarDecorator decorator = UserInterface.createToolbarDecorator(environmentTypesTable);
    decorator.setAddAction(anActionButton -> environmentTypesTable.insertRow());
    decorator.setRemoveAction(anActionButton -> environmentTypesTable.removeRow());
    decorator.setMoveUpAction(anActionButton -> environmentTypesTable.moveRowUp());
    decorator.setMoveDownAction(anActionButton -> environmentTypesTable.moveRowDown());
    decorator.addExtraAction(new AnActionButton("Revert Changes", Icons.ACTION_REVERT) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TableCellEditor cellEditor = environmentTypesTable.getCellEditor();
        if (cellEditor != null) {
          cellEditor.cancelCellEditing();
        }

        environmentTypesTable.setEnvironmentTypes(getConfiguration().getAIProviderTypes());
      }

    });
    decorator.setPreferredSize(new Dimension(-1, 400));
    JPanel panel = decorator.createPanel();
    environmentTypesTablePanel.add(panel, BorderLayout.CENTER);
    environmentTypesTable.getParent().setBackground(environmentTypesTable.getBackground());
    registerComponents(mainPanel);
  }

  @NotNull
  @Override
  public JPanel getMainComponent() {
    return mainPanel;
  }

  @Override
  public void applyFormChanges() throws ConfigurationException {
    AIProvidersGeneralSettings configuration = getConfiguration();
    AIProviderTypesTableModel model = environmentTypesTable.getModel();
    model.validate();
    AIProviderTypeBundle environmentTypeBundle = model.getAiProviderTypes();
    boolean settingsChanged = configuration.setAIProviderTypes(environmentTypeBundle);


    Project project = configuration.getProject();
    SettingsChangeNotifier.register(() -> {
      if (settingsChanged) {
        ProjectEvents.notify(project,
            EnvironmentManagerListener.TOPIC,
            (listener) -> listener.configurationChanged(project));
      }
    });
  }

  @Override
  public void resetFormChanges() {
    AIProvidersGeneralSettings settings = getConfiguration();
    environmentTypesTable.getModel().setAiProviderTypes(settings.getAIProviderTypes());
  }
}
