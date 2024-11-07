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

import com.dbn.object.common.DBObject;
import com.dbn.object.event.ObjectChangeAction;
import com.dbn.object.management.ObjectManagementAdapterBase;
import org.jetbrains.annotations.Nls;

/**
 * Abstract implementation of the {@link com.dbn.object.management.ObjectManagementAdapter} for UPDATE actions,
 * providing generic process titles and messages
 *
 * @author Dan Cioca (Oracle)
 */
public abstract class DBObjectUpdateAdapter<T extends DBObject> extends ObjectManagementAdapterBase<T> {

    public DBObjectUpdateAdapter(T object) {
        super(object, ObjectChangeAction.UPDATE);
    }

    @Nls
    @Override
    protected String getProcessTitle() {
        return txt("prc.object.title.UpdatingObject", getObjectTypeName());
    }

    @Nls
    @Override
    protected String getProcessDescription() {
        return txt("prc.object.message.UpdatingObject", getObjectTypeName(), getObjectName());
    }

    @Nls
    @Override
    protected String getSuccessMessage() {
        return txt("msg.object.info.ObjectUpdateSuccess", getObjectTypeName(), getObjectName());
    }

    @Nls
    @Override
    protected String getFailureMessage() {
        return txt("msg.object.error.ObjectUpdateFailure", getObjectTypeName(), getObjectName());
    }

}
