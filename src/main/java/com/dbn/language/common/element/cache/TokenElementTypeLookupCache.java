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

package com.dbn.language.common.element.cache;

import com.dbn.language.common.TokenType;
import com.dbn.language.common.element.impl.TokenElementType;

import java.util.Collections;
import java.util.Set;

public class TokenElementTypeLookupCache extends LeafElementTypeLookupCache<TokenElementType>{
    public TokenElementTypeLookupCache(TokenElementType elementType) {
        super(elementType);
    }

    @Override
    public boolean isFirstPossibleToken(TokenType tokenType) {
        return getTokenType() == tokenType;
    }

    protected TokenType getTokenType() {
        return elementType.tokenType;
    }

    @Override
    public boolean isFirstRequiredToken(TokenType tokenType) {
        return getTokenType() == tokenType;
    }

    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        return Collections.singleton(getTokenType());
    }

    @Override
    public void captureFirstPossibleTokens(Set<TokenType> bucket) {
        bucket.add(getTokenType());
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        return getTokenType() == tokenType;
    }

    @Override
    public boolean startsWithIdentifier() {
        return false;
    }
}