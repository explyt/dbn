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

import com.dbn.common.ui.Presentable;
import lombok.Getter;

/**
 * Secret type classification used to uniquely identify secret tokens stored in {@link com.intellij.ide.passwordSafe.PasswordSafe}
 */
@Getter
public enum SecretType implements Presentable {
    CONNECTION_PASSWORD("Connection password"),               // connection passwords
    SSH_TUNNEL_PASSWORD("SSH tunnel password"),               // password for SSH tunnels
    SSH_TUNNEL_KEY_PASSPHRASE("SSH tunnel key passphrase"),   // key passphrases for SSH tunnels
    GENERIC_CREDENTIAL("Generic credential")                  // e.g. database assistant credential tokens
    ;

    SecretType(String name) {
        this.name = name;
    }

    private final String name;
}
