package com.dbn.oracleAI.config.providers.ui;


import com.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dbn.oracleAI.config.providers.AIProviderSettings;

import javax.swing.*;
import java.awt.*;


public class AIProvidersSettingsForm extends CompositeConfigurationEditorForm<AIProviderSettings> {
  private JPanel mainPanel;
  private JPanel credentialsPanel;

  public AIProvidersSettingsForm(AIProviderSettings settings) {
    super(settings);
    credentialsPanel.add(settings.getCredentialSettings().createComponent(), BorderLayout.CENTER);
  }

  @Override
  protected JComponent getMainComponent() {
    return mainPanel;
  }
}
