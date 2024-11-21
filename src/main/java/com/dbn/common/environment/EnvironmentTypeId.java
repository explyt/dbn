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

package com.dbn.common.environment;

import com.dbn.common.constant.PseudoConstant;
import com.dbn.common.constant.PseudoConstantConverter;

import java.util.UUID;

public final class EnvironmentTypeId extends PseudoConstant<EnvironmentTypeId> {

    public static final EnvironmentTypeId DEFAULT =     get("default");
    public static final EnvironmentTypeId DEVELOPMENT = get("development");
    public static final EnvironmentTypeId INTEGRATION = get("integration");
    public static final EnvironmentTypeId PRODUCTION =  get("production");
    public static final EnvironmentTypeId OTHER =       get("other");

    public EnvironmentTypeId(String id) {
        super(id);
    }

    public static EnvironmentTypeId get(String id) {
        return PseudoConstant.get(EnvironmentTypeId.class, id);
    }

    public static EnvironmentTypeId create() {
        return EnvironmentTypeId.get(UUID.randomUUID().toString());
    }

    public static class Converter extends PseudoConstantConverter<EnvironmentTypeId> {
        public Converter() {
            super(EnvironmentTypeId.class);
        }
    }
}
