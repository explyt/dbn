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

package com.dbn.editor.code.diff;

import com.dbn.common.dispose.Failsafe;
import com.dbn.common.util.Documents;
import com.dbn.vfs.DBVirtualFileBase;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.FileDocumentContentImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SourceCodeFileContent extends FileDocumentContentImpl implements DocumentContent {
    public SourceCodeFileContent(Project project, @NotNull DBVirtualFileBase sourceCodeFile) {
        super(project, loadDocument(sourceCodeFile), sourceCodeFile);


        //boolean readonly = EnvironmentManager.getInstance(project).isReadonly(sourceCodeFile);
        //setReadOnly(readonly);
    }

    @NotNull
    private static Document loadDocument(@NotNull DBVirtualFileBase sourceCodeFile) {
        return Failsafe.nn(Documents.getDocument(sourceCodeFile));
    }


}
