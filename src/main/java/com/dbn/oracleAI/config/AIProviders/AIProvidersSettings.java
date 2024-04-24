package com.dbn.oracleAI.config.AIProviders;

import com.dbn.common.options.CompositeProjectConfiguration;
import com.dbn.common.options.Configuration;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettings;
import com.dbn.options.TopLevelConfig;
import com.dbn.oracleAI.config.AIProviders.ui.AIProvidersSettingsForm;
import com.intellij.openapi.project.Project;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(callSuper = false)
public class AIProvidersSettings
    extends CompositeProjectConfiguration<ProjectSettings, AIProvidersSettingsForm>
    implements TopLevelConfig {

  private final AIProvidersGeneralSettings generalSettings = new AIProvidersGeneralSettings(this);

  public AIProvidersSettings(ProjectSettings parent) {
    super(parent);
  }

  @NotNull
  @Override
  public AIProvidersSettingsForm createConfigurationEditor() {
    return new AIProvidersSettingsForm(this);
  }

  public static AIProvidersSettings getInstance(@NotNull Project project) {
    return ProjectSettings.get(project).getAiProvidersSettings();
  }

  @NotNull
  @Override
  public String getId() {
    return "DBNavigator.Project.AIProvidersSettings";
  }

  @Override
  public String getDisplayName() {
    return "AI Providers";
  }

  @Override
  public String getHelpTopic() {
    return "aiProvidersSettings";
  }

  @Override
  public ConfigId getConfigId() {
    return ConfigId.AI_CREDNETIALS;
  }

  @NotNull
  @Override
  public AIProvidersSettings getOriginalSettings() {
    return getInstance(getProject());
  }

  /*********************************************************
   *                     Configuration                     *
   *********************************************************/

  @Override
  protected Configuration[] createConfigurations() {
    return new Configuration[]{
        generalSettings};
  }

  @Override
  public String getConfigElementName() {
    return "ai-providers-settings";
  }
}
