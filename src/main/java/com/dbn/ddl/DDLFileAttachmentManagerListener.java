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

package com.dbn.ddl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface DDLFileAttachmentManagerListener extends EventListener {
    Topic<DDLFileAttachmentManagerListener> TOPIC = Topic.create("DDL File Attachment Event", DDLFileAttachmentManagerListener.class);

    void ddlFileDetached(Project project, VirtualFile virtualFile);

    void ddlFileAttached(Project project, VirtualFile virtualFile);
}
