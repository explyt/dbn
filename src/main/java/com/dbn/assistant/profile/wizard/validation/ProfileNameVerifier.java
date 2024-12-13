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

package com.dbn.assistant.profile.wizard.validation;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.util.Set;

import static com.dbn.nls.NlsResources.txt;

/**
 * InputVerifier class for AI profile name
 */
public class ProfileNameVerifier extends InputVerifier {
  private static final Border ERROR_BORDER = BorderFactory.createLineBorder(Color.RED, 1);
  private static final Border DEFAULT_BORDER = UIManager.getBorder("TextField.border");
  private final Set<String> profileNames;
  private final boolean isUpdate;

  public ProfileNameVerifier(Set<String> profileNames, boolean isUpdate) {
    this.profileNames = profileNames;
    this.isUpdate = isUpdate;
  }

  @Override
  public boolean verify(JComponent input) {
    JTextField textField = (JTextField) input;
    boolean isEmpty = textField.getText().trim().isEmpty();
    boolean exists = profileNames.contains(textField.getText().trim().toUpperCase());
    if (isEmpty) {
      textField.setBorder(ERROR_BORDER);
      textField.setToolTipText(txt("cfg.assistant.error.ProfileNameEmpty"));
    } else if (exists && !isUpdate) {
      textField.setBorder(ERROR_BORDER);
      textField.setToolTipText(txt("cfg.assistant.error.ProfileNameExists"));
    } else {
      textField.setBorder(DEFAULT_BORDER);
      textField.setToolTipText(null);
    }
    return !(isEmpty || exists);
  }

  @Override
  public boolean shouldYieldFocus(JComponent input) {
    return true;
  }
}
