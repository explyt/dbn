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
import com.dbn.common.thread.Background;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static com.dbn.common.component.Components.applicationService;
import static com.dbn.common.util.Commons.match;
import static com.dbn.common.util.Commons.nvl;
import static com.dbn.credentials.Secret.EMPTY;

@Slf4j
public class DatabaseCredentialManager extends ApplicationComponentBase {

    public DatabaseCredentialManager() {
        super("DBNavigator.DatabaseCredentialManager");
    }

    public static DatabaseCredentialManager getInstance() {
        return applicationService(DatabaseCredentialManager.class);
    }

    public void replaceSecrets(@NotNull Object id, Secret[] oldSecrets, Secret[] newSecrets) {
        for (int i = 0; i < oldSecrets.length; i++) {
            Secret oldSecret = oldSecrets[i];
            Secret newSecret = newSecrets[i];
            replaceSecret(id, oldSecret, newSecret);
        }
    }

    public void replaceSecret(@NotNull Object id, Secret oldSecret, Secret newSecret) {
        if (Objects.equals(oldSecret, newSecret)) return;

        String oldUser = oldSecret.getUser();
        String newUser = newSecret.getUser();

        char[] oldToken = oldSecret.getToken();
        char[] newToken = newSecret.getToken();

        boolean userChanged = !match(oldUser, newUser);
        boolean tokenChanged = !Arrays.equals(oldToken, newToken);

        if (userChanged) {
            // clear the old secret
            Secret emptySecret = new Secret(oldSecret.getType(), oldUser, EMPTY);
            uploadSecret(id, emptySecret);
        }

        if (tokenChanged) {
            uploadSecret(id, newSecret);
        }
    }

    public void uploadSecret(@NotNull Object id, @NotNull Secret secret) {
        // background update to circumvent slow-ops assertions
        Background.run(null, () -> storeSecret(id, secret));
    }

    public void storeSecret(@NotNull Object id, @NotNull Secret secret) {
        try {
            SecretType type = secret.getType();
            String user = secret.getUser();
            char[] token = secret.getToken();

            CredentialAttributes credentialAttributes = createAttributes(type, id, user);
            Credentials credentials = token.length == 0 ? null : new Credentials(user, token);

            PasswordSafe passwordSafe = PasswordSafe.getInstance();
            passwordSafe.set(credentialAttributes, credentials, false);
            log.info("Saved secret {}", secret);
        } catch (Throwable e) {
            log.error("Failed to save secret {}", secret, e);
        }
    }

    @NotNull
    public Secret loadSecret(@NotNull SecretType type, @NotNull Object id, String user) {
        Secret secret;
        try {
            CredentialAttributes credentialAttributes = createAttributes(type, id, user);

            PasswordSafe passwordSafe = PasswordSafe.getInstance();
            Credentials credentials = passwordSafe.get(credentialAttributes);
            OneTimeString password = credentials == null ? null : credentials.getPassword();
            char[] token = password == null ? EMPTY : password.toCharArray();

            secret = new Secret(type, user, token);
            log.info("Loaded secret {}", secret);

        } catch (Exception e) {
            secret = new Secret(type, user, EMPTY);
            log.error("Failed to load secret {}", secret, e);
        }
        return secret;
    }

    @NotNull
    @Compatibility
    private static CredentialAttributes createAttributes(SecretType secretType, Object secretId, String user) {
        user = nvl(user, "default");
        String serviceName = "DBNavigator." + secretType + "." + secretId;
        return new CredentialAttributes(serviceName, user, DatabaseCredentialManager.class, false);
    }

    private boolean isMemoryStorage() {
        return PasswordSafe.getInstance().isMemoryOnly();
    }
}
