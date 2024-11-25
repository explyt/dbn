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

import com.dbn.common.outcome.Outcome;
import com.dbn.common.outcome.OutcomeHandlers;
import com.dbn.common.outcome.OutcomeType;
import com.dbn.connection.context.DatabaseContext;
import com.dbn.generator.code.CodeGenerationManager;
import com.dbn.generator.code.CodeGeneratorType;
import com.dbn.generator.code.shared.CodeGenerator;
import com.dbn.generator.code.shared.CodeGeneratorInput;
import com.dbn.generator.code.shared.CodeGeneratorResult;
import lombok.Getter;

/**
 * Stub implementation of a {@link CodeGenerator}
 * @param <I> the type of {@link CodeGeneratorInput} the generator accepts
 * @param <R> the type of {@link CodeGeneratorResult} the generator produces
 *
 * @author Dan Cioca (Oracle)
 */
@Getter
public abstract class CodeGeneratorBase<I extends CodeGeneratorInput, R extends CodeGeneratorResult<I>> implements CodeGenerator<I, R> {
    private final CodeGeneratorType type;

    public CodeGeneratorBase(CodeGeneratorType type) {
        this.type = type;
        CodeGenerationManager.registerCodeGenerator(this);
    }

    @Override
    public final R generateCode(I input) {
        OutcomeHandlers outcomeHandlers = input.getOutcomeHandlers();
        try {
            R result = generateCode(input, input.getDatabaseContext());
            Outcome outcome = createOutcome(OutcomeType.SUCCESS, result, null);
            outcomeHandlers.handle(outcome);
            return result;
        } catch (Exception e){
            Outcome outcome = createOutcome(OutcomeType.FAILURE, null, e);
            outcomeHandlers.handle(outcome);
        }
        return null;
    }

    private Outcome createOutcome(OutcomeType type, R result, Exception e) {
        Outcome outcome = new Outcome(type, getTitle(type), getMessage(type), e);
        outcome.setData(result);
        return outcome;
    };

    protected abstract String getTitle(OutcomeType outcomeType);

    protected abstract String getMessage(OutcomeType outcomeType);

    protected abstract R generateCode(I input, DatabaseContext context) throws Exception;
}
