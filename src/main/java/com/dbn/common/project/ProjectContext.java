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

package com.dbn.common.project;

import com.intellij.openapi.project.Project;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

public class ProjectContext {
    private static final ThreadLocal<ProjectRef> local = new ThreadLocal<>();

    public static void surround(Project project, Runnable runnable) {
        try {
            local.set(ProjectRef.of(project));
            runnable.run();
        } finally {
            local.remove();
        }
    }

    @SneakyThrows
    public static <T> T surround(Project project, Callable<T> callable) {
        try {
            local.set(ProjectRef.of(project));
            return callable.call();
        } finally {
            local.remove();
        }
    }

    @Nullable
    public static Project getProject() {
        return ProjectRef.get(local.get());
    }
}
