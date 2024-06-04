package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Profile edition provider step for edition wizard
 *
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionProviderStep extends WizardStep<ProfileEditionWizardModel> {

  private JPanel profileEditionProviderMainPane;
  private JComboBox<ProviderType> providerNameCombo;
  private JLabel providerNameLabel;
  private JLabel providerModelLabel;
  private JComboBox<ProviderModel> providerModelCombo;
  private JSlider temperatureSlider;
  private Profile profile;

  private final int MIN_TEMPERATURE = 0;
  private final int MAX_TEMPERATURE = 10;
  private final int DEFAULT_TEMPERATURE = 5;


  public ProfileEditionProviderStep(Project project, @Nullable Profile profile, boolean isUpdate) {
    super(ResourceBundle.getBundle("Messages", Locale.getDefault()).getString("profile.mgmt.provider_step.title"),
            ResourceBundle.getBundle("Messages", Locale.getDefault()).getString("profile.mgmt.provider_step.explaination"),
            AllIcons.General.Settings);
    this.profile = profile;
    configureTemperatureSlider();
    populateCombos();
    if (isUpdate) {
      providerNameCombo.setSelectedItem(profile.getProvider());
      providerModelCombo.setSelectedItem(profile.getModel() != null ? profile.getModel() : profile.getProvider().getDefaultModel());
      temperatureSlider.setValue((int) (profile.getTemperature() * 10));
    }
  }

  private void populateCombos() {
    for (ProviderType type : ProviderType.values()) {
      providerNameCombo.addItem(type);
    }
    ((ProviderType) providerNameCombo.getSelectedItem()).getModels().forEach(m -> providerModelCombo.addItem(m));
    providerNameCombo.addActionListener((e) -> {
      providerModelCombo.removeAllItems();
      ((ProviderType) providerNameCombo.getSelectedItem()).getModels().forEach(m -> providerModelCombo.addItem(m));
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
    return profileEditionProviderMainPane;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return null;
  }

  @Override
  public WizardStep<ProfileEditionWizardModel> onNext(ProfileEditionWizardModel model) {
    profile.setProvider(ProviderType.valueOf(Objects.requireNonNull(providerNameCombo.getSelectedItem()).toString()));
    profile.setModel((ProviderModel) providerModelCombo.getSelectedItem());
    profile.setTemperature((float) temperatureSlider.getValue() / 10);
    return super.onNext(model);
  }
}
