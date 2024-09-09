package com.dbn.oracleAI.config.providers;

import com.dbn.common.options.CompositeProjectConfiguration;
import com.dbn.common.options.Configuration;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettings;
import com.dbn.options.TopLevelConfig;
import com.dbn.oracleAI.config.providers.ui.AIProvidersSettingsForm;
import com.intellij.openapi.project.Project;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(callSuper = false)
public class AIProviderSettings
    extends CompositeProjectConfiguration<ProjectSettings, AIProvidersSettingsForm>
    implements TopLevelConfig {

  private final AIProviderCredentialSettings credentialSettings = new AIProviderCredentialSettings(this);

  public AIProviderSettings(ProjectSettings parent) {
    super(parent);
  }

  @NotNull
  @Override
  public AIProvidersSettingsForm createConfigurationEditor() {
    return new AIProvidersSettingsForm(this);
  }

  public static AIProviderSettings getInstance(@NotNull Project project) {
    return ProjectSettings.get(project).getAiProviderSettings();
  }

  @NotNull
  @Override
  public String getId() {
    return "DBNavigator.Project.AIProvidersSettings";
  }

  @Override
  public String getDisplayName() {
    return txt("ai.providers.settings.window.title");
  }

  @Override
  public String getHelpTopic() {
    return "aiProvidersSettings";
  }

  @Override
  public ConfigId getConfigId() {
    return ConfigId.AI_PROVIDERS;
  }

  @NotNull
  @Override
  public AIProviderSettings getOriginalSettings() {
    return getInstance(getProject());
  }

  /*********************************************************
   *                     Configuration                     *
   *********************************************************/

  @Override
  protected Configuration[] createConfigurations() {
    return new Configuration[]{
            credentialSettings};
  }

  @Override
  public String getConfigElementName() {
    return "ai-provider-settings";
  }
}
