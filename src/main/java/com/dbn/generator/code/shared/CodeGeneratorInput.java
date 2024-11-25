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

package com.dbn.generator.code.shared;

import com.dbn.common.outcome.OutcomeHandler;
import com.dbn.common.outcome.OutcomeHandlers;
import com.dbn.common.outcome.OutcomeType;
import com.dbn.connection.context.DatabaseContext;

/**
 * Input for the {@link CodeGenerator}, containing all necessary information for code generation to be performed
 *
 * @author Dan Cioca (Oracle)
 */
public interface CodeGeneratorInput {
    DatabaseContext getDatabaseContext();

    void addOutcomeHandler(OutcomeType outcomeType, OutcomeHandler handler);

    OutcomeHandlers getOutcomeHandlers();
}
