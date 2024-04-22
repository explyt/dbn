package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.project.Project;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.util.Hashtable;
import java.util.Objects;

/**
 * Profile edition provider step for edition wizard
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionProviderStep extends AbstractProfileEditionStep  {

  private JPanel profileEditionProviderMainPane;
  private JComboBox providerNameCombo;
  private JLabel providerNameLabel;
  private JLabel providerModelLabel;
  private JComboBox providerModelCombo;
  private JSlider temperatureSlider;


  private final int MIN_TEMPERATURE = 0;
  private final int MAX_TEMPERATURE = 10;
  private final int DEFAULT_TEMPERATURE = 5;

  public ProfileEditionProviderStep(Project project) {
    super();
    configureTemperatureSlider();

  }
  public ProfileEditionProviderStep(Project project, Profile profile) {
    super();
    configureTemperatureSlider();
    providerNameCombo.setSelectedItem(profile.getProvider().toString().toUpperCase());
    if(profile.getModel()!=null) providerModelCombo.setSelectedItem(profile.getModel());
    temperatureSlider.setValue((int) (profile.getTemperature()*10));
  }

  private void configureTemperatureSlider(){
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
    labelTable.put(currentValue, new JLabel(String.valueOf((float) currentValue/10)));
    labelTable.put(10, new JLabel("1"));
    slider.setLabelTable(labelTable);
  }

  @Override public JPanel getPanel() {
    return profileEditionProviderMainPane;
  }

  @Override public void setAttributesOn(Profile p) {
    p.setProvider(ProviderType.valueOf(Objects.requireNonNull(providerNameCombo.getSelectedItem()).toString()));
    p.setModel(Objects.requireNonNull(providerModelCombo.getSelectedItem()).toString());
    p.setTemperature((double) temperatureSlider.getValue()/10);
  }

}
