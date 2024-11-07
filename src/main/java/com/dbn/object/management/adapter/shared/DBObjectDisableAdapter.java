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

package com.dbn.object.management.adapter.shared;

import com.dbn.common.Priority;
import com.dbn.common.outcome.BasicOutcomeHandler;
import com.dbn.common.outcome.OutcomeType;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.event.ObjectChangeAction;
import com.dbn.object.management.ObjectManagementAdapterBase;
import org.jetbrains.annotations.Nls;

import static com.dbn.object.common.status.DBObjectStatus.ENABLED;

/**
 * Abstract implementation of the {@link com.dbn.object.management.ObjectManagementAdapter} for DISABLE actions, 
 * providing generic process titles and messages
 *
 * @author Dan Cioca (Oracle)
 */
public abstract class DBObjectDisableAdapter<T extends DBSchemaObject> extends ObjectManagementAdapterBase<T> {

    public DBObjectDisableAdapter(T object) {
        super(object, ObjectChangeAction.DISABLE);
        addOutcomeHandler(OutcomeType.SUCCESS, BasicOutcomeHandler.create(Priority.HIGH, o -> disableLocal()));
    }

    private void disableLocal() {
        getObject().getStatus().set(ENABLED, false);
    }

    @Nls
    @Override
    protected String getProcessTitle() {
        return txt("prc.object.title.DisablingObject", getObjectTypeName());
    }

    @Nls
    @Override
    protected String getProcessDescription() {
        return txt("prc.object.message.DisablingObject", getObjectTypeName(), getObjectName());
    }

    @Nls
    @Override
    protected String getSuccessMessage() {
        return txt("msg.object.info.ObjectDisableSuccess", getObjectTypeName(), getObjectName());
    }

    @Nls
    @Override
    protected String getFailureMessage() {
        return txt("msg.object.error.ObjectDisableFailure", getObjectType(), getObjectName());
    }
}
