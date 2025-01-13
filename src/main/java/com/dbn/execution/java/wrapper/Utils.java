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

package com.dbn.execution.java.wrapper;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.util.Properties;

import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public class Utils {
	public static String parseTemplate(String templateName, Properties properties, Project project){
		FileTemplateManager templateManager = FileTemplateManager.getInstance(project);
		FileTemplate template = templateManager.getCodeTemplate(templateName);

		try {
			return template.getText(properties);
		} catch (IOException ex) {
			conditionallyLog(ex);
			return "";
		}
	}
}
