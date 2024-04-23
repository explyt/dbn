package com.dbn.oracleAI.config.AIProviders;

import com.dbn.browser.options.BrowserDisplayMode;
import com.dbn.common.options.BasicProjectConfiguration;
import com.dbn.oracleAI.config.AIProviders.ui.AIProvidersGeneralSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.dbn.common.options.setting.Settings.newElement;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class AIProvidersGeneralSettings
    extends BasicProjectConfiguration<AIProvidersSettings, AIProvidersGeneralSettingsForm> {

  private BrowserDisplayMode displayMode = BrowserDisplayMode.TABBED;
  private AIProviderTypeBundle aiProviderTypes = new AIProviderTypeBundle(AIProviderTypeBundle.DEFAULT);

  AIProvidersGeneralSettings(AIProvidersSettings parent) {
    super(parent);
  }

  public AIProviderTypeBundle getAIProviderTypes() {
    return aiProviderTypes;
  }

  public boolean setAIProviderTypes(AIProviderTypeBundle aiProviderTypes) {
    boolean changed = !Objects.equals(this.aiProviderTypes, aiProviderTypes);
    this.aiProviderTypes = new AIProviderTypeBundle(aiProviderTypes);
    return changed;
  }

  @NotNull
  @Override
  public AIProvidersGeneralSettingsForm createConfigurationEditor() {
    return new AIProvidersGeneralSettingsForm(this);
  }

  @Override
  public String getConfigElementName() {
    return "ai-provider";
  }

  @Override
  public void readConfiguration(Element element) {
    Element environmentTypesElement = element.getChild("ai-provider-types");
    if (environmentTypesElement != null) {
      aiProviderTypes.clear();
      for (Element child : environmentTypesElement.getChildren()) {
        AIProviderType environmentType = new AIProviderType(null);
        environmentType.readConfiguration(child);
        aiProviderTypes.add(environmentType);
      }
    }
  }

  @Override
  public void writeConfiguration(Element element) {
    Element environmentTypesElement = newElement(element, "ai-provider-types");
    for (AIProviderType environmentType : aiProviderTypes) {
      Element itemElement = newElement(environmentTypesElement, "ai-provider-type");
      environmentType.writeConfiguration(itemElement);
    }
  }
}