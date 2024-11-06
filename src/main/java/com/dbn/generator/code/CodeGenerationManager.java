/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.generator.code;

import com.dbn.common.component.ProjectComponentBase;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.component.Components.projectService;

public class CodeGenerationManager extends ProjectComponentBase {

    public static final String COMPONENT_NAME = "DBNavigator.Project.CodeGenerationManager";

    private CodeGenerationManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static CodeGenerationManager getInstance(@NotNull Project project) {
        return projectService(project, CodeGenerationManager.class);
    }

    public void generateCode(CodeGeneratorType type) {

    }

}
