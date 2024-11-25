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

package com.dbn.generator.code.shared.base;

import com.dbn.common.outcome.OutcomeHandler;
import com.dbn.common.outcome.OutcomeHandlers;
import com.dbn.common.outcome.OutcomeHandlersImpl;
import com.dbn.common.outcome.OutcomeType;
import com.dbn.common.ref.WeakRef;
import com.dbn.connection.context.DatabaseContext;
import com.dbn.generator.code.shared.CodeGeneratorInput;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class CodeGeneratorInputBase implements CodeGeneratorInput {
    private final WeakRef<DatabaseContext> databaseContext;
    private final OutcomeHandlers outcomeHandlers = new OutcomeHandlersImpl();
    // TODO consider making CodeGenerators stateful (e.g. add factories) and move outcome handlers to them

    protected CodeGeneratorInputBase(DatabaseContext databaseContext) {
        this.databaseContext = WeakRef.of(databaseContext);
    }

    @NotNull
    public DatabaseContext getDatabaseContext() {
        return WeakRef.ensure(databaseContext);
    }

    @Override
    public final void addOutcomeHandler(OutcomeType outcomeType, OutcomeHandler handler) {
        if (handler == null) return;
        outcomeHandlers.addHandler(outcomeType, handler);
    }
}
