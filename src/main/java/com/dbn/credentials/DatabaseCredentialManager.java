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

package com.dbn.credentials;

import com.dbn.common.compatibility.Compatibility;
import com.dbn.common.component.ApplicationComponentBase;
import com.dbn.common.database.AuthenticationInfo;
import com.dbn.common.thread.Background;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import org.jetbrains.annotations.NotNull;

import java.net.PasswordAuthentication;
import java.util.Arrays;

import static com.dbn.common.component.Components.applicationService;
import static com.dbn.common.database.AuthenticationInfo.EMPTY_PASSWORD;
import static com.dbn.common.util.Commons.match;
import static com.dbn.common.util.Commons.nvl;

public class DatabaseCredentialManager extends ApplicationComponentBase {

    public DatabaseCredentialManager() {
        super("DBNavigator.DatabaseCredentialManager");
    }

    public static DatabaseCredentialManager getInstance() {
        return applicationService(DatabaseCredentialManager.class);
    }

    public void replacePassword(@NotNull CredentialServiceType serviceType, @NotNull Object serviceId, PasswordAuthentication oldAuth, PasswordAuthentication newAuth) {
        String oldUserName = oldAuth.getUserName();
        char[] oldPassword = oldAuth.getPassword();

        String newUserName = newAuth.getUserName();
        char[] newPassword = newAuth.getPassword();

        boolean userNameChanged = !match(oldUserName, newUserName);
        boolean passwordChanged = !Arrays.equals(oldPassword, newPassword);
        if (userNameChanged || passwordChanged) {

            if (userNameChanged) {
                // clear the old authentication data
                oldAuth = new PasswordAuthentication(oldUserName, EMPTY_PASSWORD);
                uploadPassword(serviceType, serviceId, oldAuth);
            }

            if (passwordChanged) {
                uploadPassword(serviceType, serviceId, newAuth);
            }
        }
    }

    public void uploadPassword(@NotNull CredentialServiceType serviceType, @NotNull Object serviceId, @NotNull PasswordAuthentication auth) {
        // background update to circumvent slow-ops assertions
        Background.run(null, () -> storePassword(serviceType, serviceId, auth));
    }

    public void storePassword(@NotNull CredentialServiceType serviceType, @NotNull Object serviceId, @NotNull PasswordAuthentication auth) {
        String userName = auth.getUserName();
        char[] password = auth.getPassword();

        CredentialAttributes credentialAttributes = createAttributes(serviceType, serviceId, userName);
        Credentials credentials = password.length == 0 ? null : new Credentials(userName, password);

        PasswordSafe passwordSafe = PasswordSafe.getInstance();
        passwordSafe.set(credentialAttributes, credentials, false);
    }

    public char[] loadPassword(@NotNull CredentialServiceType serviceType, @NotNull Object serviceId, String userName) {
        CredentialAttributes credentialAttributes = createAttributes(serviceType, serviceId, userName);

        PasswordSafe passwordSafe = PasswordSafe.getInstance();
        Credentials credentials = passwordSafe.get(credentialAttributes);
        OneTimeString password = credentials == null ? null : credentials.getPassword();
        return password == null ? AuthenticationInfo.EMPTY_PASSWORD : password.toCharArray() ;
    }

    @NotNull
    @Compatibility
    private static CredentialAttributes createAttributes(CredentialServiceType serviceType, Object serviceId, String userName) {
        userName = nvl(userName, "default");
        String service = "DBNavigator." + serviceType + "." + serviceId;
        return new CredentialAttributes(service, userName, DatabaseCredentialManager.class, false);
    }

    private boolean isMemoryStorage() {
        return PasswordSafe.getInstance().isMemoryOnly();
    }
}
