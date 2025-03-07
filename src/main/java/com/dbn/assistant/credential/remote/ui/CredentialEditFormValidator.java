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

package com.dbn.assistant.credential.remote.ui;

import com.dbn.common.ref.WeakRef;
import com.dbn.nls.NlsSupport;
import com.dbn.object.type.DBCredentialType;
import com.intellij.openapi.ui.ValidationInfo;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Validator for Credential editors
 * (code isolated from {@link CredentialEditForm})
 *
 * @author Ayoub Aarrasse (Oracle)
 * @deprecated replace with {@link com.dbn.common.ui.form.DBNFormValidator}
 */
public class CredentialEditFormValidator implements NlsSupport {

    private final WeakRef<CredentialEditForm> form;

    public CredentialEditFormValidator(CredentialEditForm form) {
        this.form = WeakRef.of(form);
    }

    private CredentialEditForm getForm() {
        return form.ensure();
    }

    public ValidationInfo validate() {
        CredentialEditForm form = getForm();

        JTextField credentialNameField = form.getCredentialNameField();
        String credentialName = credentialNameField.getText();
        if (credentialName.isEmpty()) {
            return new ValidationInfo(txt("cfg.assistant.error.CredentialNameEmpty"), credentialNameField);
        }
        if (form.getUsedCredentialNames().contains(credentialName.toUpperCase()) && form.getCredential() == null) {
            return new ValidationInfo(txt("cfg.assistant.error.CredentialNameExists"),
                    credentialNameField);
        }
        if (credentialNameField.isEnabled()) {
            DBCredentialType credentialType = (DBCredentialType) form.getCredentialTypeComboBox().getSelectedItem();
            if (credentialType == null) return new ValidationInfo("Please select a credential type"); // TODO NLS
            switch (credentialType) {
                case PASSWORD:
                    return validatePasswordCredential();
                case OCI:
                    return validateOciCredential();
            }
        }

        return null;
    }

    private ValidationInfo validateOciCredential() {
        CredentialEditForm form = getForm();

        JTextField userOcidField = form.getOciCredentialUserOcidField();
        String userOcid = userOcidField.getText();
        if (userOcid.isEmpty()) {
            return new ValidationInfo(txt("cfg.assistant.error.UserOcidEmpty"), userOcidField);
        }
        if (!userOcid.startsWith("ocid1.user.oc1.")) {
            return new ValidationInfo(txt("cfg.assistant.error.UserOcidInvalid"), userOcidField);
        }
        JTextField userTenancyOcidField = form.getOciCredentialUserTenancyOcidField();
        String userTenancyOcid = userTenancyOcidField.getText();
        if (userTenancyOcid.isEmpty()) {
            return new ValidationInfo(txt("cfg.assistant.error.UserTenancyOcidEmpty"), userTenancyOcidField);
        }
        if (!userTenancyOcid.startsWith("ocid1.tenancy.oc1.")) {
            return new ValidationInfo(txt("cfg.assistant.error.UserTenancyOcidInvalid"), userTenancyOcidField);
        }
        JTextField fingerprintField = form.getOciCredentialFingerprintField();
        if (fingerprintField.getText().isEmpty()) {
            return new ValidationInfo(txt("cfg.assistant.error.FingerprintEmpty"), fingerprintField);
        }
        JTextField privateKeyField = form.getOciCredentialPrivateKeyField();
        if (privateKeyField.getText().isEmpty()) {
            return new ValidationInfo(txt("cfg.assistant.error.PrivateKeyEmpty"), privateKeyField);
        }
        return null;
    }

    private ValidationInfo validatePasswordCredential() {
        CredentialEditForm form = getForm();

        JTextField usernameField = form.getPasswordCredentialUsernameField();
        if (usernameField.getText().isEmpty()) {
            return new ValidationInfo(txt("cfg.assistant.error.UserNameEmpty"), usernameField);
        }
        JPasswordField passwordField = form.getPasswordCredentialPasswordField();
        if (passwordField.getText().isEmpty()) {
            return new ValidationInfo(txt("cfg.assistant.error.PasswordEmpty"), passwordField);
        }
        return null;
    }
}
