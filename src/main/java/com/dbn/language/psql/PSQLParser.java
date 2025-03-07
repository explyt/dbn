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

package com.dbn.language.psql;

import com.dbn.language.common.DBLanguageDialect;
import com.dbn.language.common.DBLanguageParser;
import org.jetbrains.annotations.NonNls;

public class PSQLParser extends DBLanguageParser {
    public PSQLParser(DBLanguageDialect languageDialect, @NonNls String tokenTypesFile, @NonNls String elementTypesFile, @NonNls String defaultParseRootId) {
        super(languageDialect, tokenTypesFile, elementTypesFile, defaultParseRootId);
    }

/*
    @NotNull
    @Override
    public ASTNode parse(IElementType rootElementType, PsiBuilder builder, String parseRootId) {
        if (DatabaseNavigator.getInstance().isTempPSQLParsingEnabled()) {
            return super.parse(rootElementType, builder, parseRootId);
        } else {
            PsiBuilder.Marker marker = builder.mark();
            boolean advancedLexer = false;
            while (!builder.eof()) {
                builder.advanceLexer();
                advancedLexer = true;
            }
            if (!advancedLexer) builder.advanceLexer();
            marker.done(rootElementType);
            return builder.getTreeBuilt();
        }
    }
*/
}