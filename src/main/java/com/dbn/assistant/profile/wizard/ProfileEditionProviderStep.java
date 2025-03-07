/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.assistant.profile.wizard;

import com.dbn.assistant.provider.AIModel;
import com.dbn.assistant.provider.AIProvider;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.common.util.Lists;
import com.dbn.common.util.Strings;
import com.dbn.connection.ConnectionHandler;
import com.intellij.openapi.Disposable;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import static com.dbn.common.util.Commons.nvl;
import static com.dbn.nls.NlsResources.txt;

/**
 * Profile edition provider step for edition wizard
 *
 * @see ProfileEditionWizard
 */
public class ProfileEditionProviderStep extends WizardStep<ProfileEditionWizardModel>  implements Disposable {

  private JPanel mainPanel;
  private JComboBox<AIProvider> providerNameCombo;
  private JComboBox<AIModel> providerModelCombo;
  private JSlider temperatureSlider;
  private final ProfileData profile;

  private static final int MIN_TEMPERATURE = 0;
  private static final int MAX_TEMPERATURE = 10;
  private static final int DEFAULT_TEMPERATURE = 5;


  public ProfileEditionProviderStep(ConnectionHandler connection, ProfileData profile, boolean isUpdate) {
    super(txt("cfg.assistant.title.ProviderSettings"),
            txt("cfg.assistant.text.ProviderSettings"));
    this.profile = profile;
    configureTemperatureSlider();
    populateCombos();
    if (isUpdate) {
      providerNameCombo.setSelectedItem(profile.getProvider());
      providerModelCombo.setSelectedItem(profile.getModel() != null ? profile.getModel() : profile.getProvider().getDefaultModel());
      temperatureSlider.setValue((int) (profile.getTemperature() * 10));
    } else {
      UserInterface.whenShown(mainPanel, () -> {
        AIProvider provider = guessProviderType(profile);
        providerNameCombo.setSelectedItem(provider);
        providerModelCombo.setSelectedItem(provider.getDefaultModel());
        temperatureSlider.setValue(5);
      }, false);

    }
  }

  private AIProvider guessProviderType(ProfileData profile) {
    Set<String> captions = new HashSet<>();
    captions.add(nvl(profile.getName(), ""));
    captions.add(nvl(profile.getCredentialName(), ""));
    captions.add(nvl(profile.getDescription(), ""));

    for (AIProvider value : AIProvider.values()) {
        if (captions.stream().anyMatch(c -> Strings.containsIgnoreCase(c, value.getId()))) return value;
    }
    return Lists.firstElement(AIProvider.values());
  }

  private void populateCombos() {
    for (AIProvider type : AIProvider.values()) {
      providerNameCombo.addItem(type);
    }
    ((AIProvider) providerNameCombo.getSelectedItem()).getModels().forEach(m -> providerModelCombo.addItem(m));
    providerNameCombo.addActionListener((e) -> {
      providerModelCombo.removeAllItems();
      ((AIProvider) providerNameCombo.getSelectedItem()).getModels().forEach(m -> providerModelCombo.addItem(m));
    });
  }

  private void configureTemperatureSlider() {
    temperatureSlider.setMinimum(MIN_TEMPERATURE);
    temperatureSlider.setMaximum(MAX_TEMPERATURE);
    temperatureSlider.setValue(DEFAULT_TEMPERATURE);
    temperatureSlider.setMajorTickSpacing(2);
    temperatureSlider.setMinorTickSpacing(1);
    temperatureSlider.setPaintTicks(true);
    temperatureSlider.setPaintLabels(true);
    updateSliderLabels(temperatureSlider, temperatureSlider.getValue());

    temperatureSlider.addChangeListener(e -> {
      JSlider source = (JSlider) e.getSource();
      if (!source.getValueIsAdjusting()) {
        updateSliderLabels(source, source.getValue());
      }
    });

  }

  private void updateSliderLabels(JSlider slider, int currentValue) {
    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
    labelTable.put(0, new JLabel("0"));
    labelTable.put(currentValue, new JLabel(String.valueOf((float) currentValue / 10)));
    labelTable.put(10, new JLabel("1"));
    slider.setLabelTable(labelTable);
  }

  @Override
  public @Nullable String getHelpId() {
    return null;
  }

  @Override
  public JComponent prepare(WizardNavigationState wizardNavigationState) {
    return mainPanel;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return providerNameCombo;
  }

  @Override
  public WizardStep<ProfileEditionWizardModel> onNext(ProfileEditionWizardModel wizardModel) {
    AIProvider provider = (AIProvider) providerNameCombo.getSelectedItem();
    AIModel model = (AIModel) providerModelCombo.getSelectedItem();
    profile.setProvider(provider);
    profile.setModel(model);
    profile.setTemperature((float) temperatureSlider.getValue() / 10);
    return super.onNext(wizardModel);
  }

  @Override
  public void dispose() {
    // TODO dispose UI resources
  }
}
