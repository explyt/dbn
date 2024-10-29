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

package com.dbn.object.management;

import com.dbn.assistant.credential.remote.CredentialManagementService;
import com.dbn.assistant.profile.ProfileManagementService;
import com.dbn.common.outcome.OutcomeHandler;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.event.ObjectChangeAction;
import com.dbn.object.type.DBObjectType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.util.Unsafe.cast;

public interface ObjectManagementService<T extends DBObject> {

    static <T extends DBSchemaObject> ObjectManagementService<T> get(@NotNull DBObject object) {
        return get(object.getProject(), object.getObjectType());
    }

    static <T extends DBSchemaObject> ObjectManagementService<T> get(@NotNull Project project, @NotNull DBObjectType objectType) {
        switch (objectType) {
            case AI_PROFILE: return cast(ProfileManagementService.getInstance(project));
            case CREDENTIAL: return cast(CredentialManagementService.getInstance(project));
            default: return null;
        }
    }

    void createObject(T object, OutcomeHandler successHandler);

    void updateObject(T object, OutcomeHandler successHandler);

    void deleteObject(T object, OutcomeHandler successHandler);

    void enableObject(T object, OutcomeHandler successHandler);

    void disableObject(T object, OutcomeHandler successHandler);

    void changeObject(T object, ObjectChangeAction action, OutcomeHandler successHandler);
}
