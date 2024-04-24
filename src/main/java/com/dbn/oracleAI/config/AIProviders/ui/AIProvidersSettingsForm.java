package com.dbn.oracleAI.config.AIProviders.ui;


import com.dbn.common.environment.options.ui.EnvironmentTypesEditorTable;
import com.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dbn.oracleAI.config.AIProviders.AIProvidersSettings;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;


public class AIProvidersSettingsForm extends CompositeConfigurationEditorForm<AIProvidersSettings> {
  private JPanel mainPanel;
  private JCheckBox connectionTabsCheckBox;
  private JCheckBox objectEditorTabsCheckBox;
  private JCheckBox scriptEditorTabsCheckBox;
  private JCheckBox dialogHeadersCheckBox;
  private JCheckBox executionResultTabsCheckBox;
  private JPanel environmentTypesPanel;
  private JPanel environmentApplicabilityPanel;
  private JPanel environmentTypesTablePanel;
  private EnvironmentTypesEditorTable environmentTypesTable;
  private JPanel generalPanel;

  public AIProvidersSettingsForm(AIProvidersSettings settings) {
    super(settings);
    generalPanel.add(settings.getGeneralSettings().createComponent(), BorderLayout.CENTER);
  }

  @Override
  protected JComponent getMainComponent() {
    return mainPanel;
  }
}
