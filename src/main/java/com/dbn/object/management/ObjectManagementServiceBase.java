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

import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.outcome.OutcomeHandler;
import com.dbn.common.outcome.OutcomeType;
import com.dbn.object.common.DBObject;
import com.dbn.object.event.ObjectChangeAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.exception.Exceptions.unsupported;
import static com.dbn.object.event.ObjectChangeAction.CREATE;
import static com.dbn.object.event.ObjectChangeAction.DELETE;
import static com.dbn.object.event.ObjectChangeAction.DISABLE;
import static com.dbn.object.event.ObjectChangeAction.ENABLE;
import static com.dbn.object.event.ObjectChangeAction.UPDATE;

/**
 * Generic database object management component
 * Exposes CRUD-like actions for the {@link T} entities
 * Internally instantiates the specialized {@link com.dbn.object.management.ObjectManagementAdapter} component,
 * and invokes the MODAL option of the adapter
 *
 * @author Dan Cioca (Oracle)
 */
public abstract class ObjectManagementServiceBase<T extends DBObject> extends ProjectComponentBase implements ObjectManagementService<T> {
    protected ObjectManagementServiceBase(@NotNull Project project, String componentName) {
        super(project, componentName);
    }

    public final void createObject(T object, OutcomeHandler successHandler) {
        invokeModal(object, CREATE, successHandler);
    }

    public final void updateObject(T object, OutcomeHandler successHandler) {
        invokeModal(object, UPDATE, successHandler);
    }

    public final void deleteObject(T object, OutcomeHandler successHandler) {
        invokeModal(object, DELETE, successHandler);
    }

    public final void enableObject(T object, OutcomeHandler successHandler) {
        invokeModal(object, ENABLE, successHandler);
    }

    public final void disableObject(T object, OutcomeHandler successHandler) {
        invokeModal(object, DISABLE, successHandler);
    }

    @Override
    public final void changeObject(T object, ObjectChangeAction action, OutcomeHandler successHandler) {
        switch (action) {
            case CREATE: createObject(object, successHandler); break;
            case UPDATE: updateObject(object, successHandler); break;
            case DELETE: deleteObject(object, successHandler); break;
            case ENABLE: enableObject(object, successHandler); break;
            case DISABLE: disableObject(object, successHandler); break;
            default: unsupported(action);
        }
    }

    private void invokeModal(T object, ObjectChangeAction action, OutcomeHandler successHandler) {
        ObjectManagementAdapterBase<T> adapter = createAdapter(object, action);
        if (adapter == null) return;

        adapter.addOutcomeHandler(OutcomeType.SUCCESS, successHandler);
        adapter.invokeModal();
    }

    @Nullable
    protected abstract ObjectManagementAdapterBase<T> createAdapter(T object, ObjectChangeAction action);
}
