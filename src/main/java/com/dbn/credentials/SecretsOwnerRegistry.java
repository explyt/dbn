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

import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.WeakList;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * A registry for managing and retrieving {@link SecretsOwner} instances. This class maintains
 * a weakly referenced list of secrets owners, allowing the retrieval of
 * the name associated with a secret owner via their identifier.
 */
@Slf4j
public class SecretsOwnerRegistry {
    private static final WeakList<SecretsOwner> DATA = new WeakList<>();
    private static final Map<Object, SecretsOwner> DATA_CACHE = ContainerUtil.createConcurrentWeakValueMap();

    public static void register(SecretsOwner secretsOwner) {
        DATA.add(secretsOwner);
    }

    public static String getOwnerName(Object ownerId) {
        SecretsOwner secretsOwner = DATA_CACHE.computeIfAbsent(ownerId, id -> findSecretsOwner(id));
        if (secretsOwner == NULL) {
            DATA_CACHE.remove(ownerId);
            return Objects.toString(ownerId);
        }

        return secretsOwner.getSecretOwnerName();
    }

    private static SecretsOwner findSecretsOwner(Object ownerId) {
        for (SecretsOwner secretsOwner : DATA) {
            if (Objects.equals(secretsOwner.getSecretOwnerId(), ownerId)) {
                return secretsOwner;
            }
        }
        log.error("No secrets owner found for id: {}", ownerId);
        return NULL;
    }

    private static final SecretsOwner NULL = new SecretsOwner() {
        @NotNull
        @Override
        public Object getSecretOwnerId() {
            return "NULL";
        }

        @Override
        public String getSecretOwnerName() {
            return "NULL";
        }

        @Override
        public @NotNull Secret[] getSecrets() {
            return new Secret[0];
        }

        @Override
        public void initSecrets() {

        }
    };
}
