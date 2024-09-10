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

import com.dbn.common.component.ConnectionComponent;
import com.dbn.common.notification.NotificationGroup;
import com.dbn.common.outcome.*;
import com.dbn.common.thread.Progress;
import com.dbn.common.util.Named;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dbn.diagnostics.Diagnostics;
import com.dbn.object.event.ObjectChangeAction;
import com.dbn.object.event.ObjectChangeNotifier;
import com.dbn.object.type.DBObjectType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nls;

import java.sql.SQLException;

import static com.dbn.common.Priority.HIGHEST;

/**
 * Abstract base implementation of an {@link ObjectManagementAdapter}
 * Forces the actual adapter implementers to provide the logic for the actual {@link com.dbn.database.interfaces.DatabaseInterface} interaction,
 * as well as the various titles and captions to be displayed in the progress elements and outcome confirmation messages
 * @param <T> the type of the entity being handled by the adapter
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
@Getter
@Setter
public abstract class ObjectManagementAdapterBase<T extends Named> extends ConnectionComponent implements ObjectManagementAdapter<T> {
    private final ObjectChangeAction action;
    private final DBObjectType objectType;
    private final OutcomeHandlers outcomeHandlers = new OutcomeHandlersImpl();

    public ObjectManagementAdapterBase(ConnectionHandler connection, DBObjectType objectType, ObjectChangeAction action) {
        super(connection);
        this.objectType = objectType;
        this.action = action;

        outcomeHandlers.addHandler(OutcomeType.SUCCESS, ObjectChangeNotifier.create(connection, objectType, action));
        outcomeHandlers.addNotificationHandler(OutcomeType.SUCCESS, getProject(), NotificationGroup.ASSISTANT);
        outcomeHandlers.addMessageHandler(OutcomeType.FAILURE, getProject());
    }

    @Override
    public final void addOutcomeHandler(OutcomeType outcomeType, OutcomeHandler handler) {
        if (handler == null) return;
        outcomeHandlers.addHandler(outcomeType, handler);
    }

    @Override
    public final void invokeModal(T entity) {
        Progress.modal(getProject(), getConnection(), true,
                getProcessTitle(),
                getProcessDescription(entity),
                progress -> invoke(entity));
    }

    @Override
    public void invokePrompted(T entity) {
        Progress.prompt(getProject(), getConnection(), true,
                getProcessTitle(),
                getProcessDescription(entity),
                progress -> invoke(entity));
    }

    @Override
    public final void invokeInBackground(T entity) {
        Progress.background(getProject(), getConnection(), true,
                getProcessTitle(),
                getProcessDescription(entity),
                progress -> invoke(entity));
    }

    public final void invoke(T entity) {
        try {
            DatabaseInterfaceInvoker.execute(HIGHEST,
                    getProcessTitle(),
                    getProcessDescription(entity),
                    getProject(),
                    getConnectionId(),
                    conn -> invokeDatabaseInterface(getConnection(), conn, entity));

            handleSuccess(entity);
        } catch (Exception e) {
            Diagnostics.conditionallyLog(e);
            handleFailure(entity, e);
        }
    }

    protected void handleSuccess(T entity) {
        Outcome outcome = Outcomes.success(getSuccessTitle(), getSuccessMessage(entity));
        outcomeHandlers.handle(outcome);
    }

    protected void handleFailure(T entity, Exception e) {
        Outcome outcome = Outcomes.failure(getFailureTitle(), getFailureMessage(entity), e);
        outcomeHandlers.handle(outcome);
    }

    protected abstract void invokeDatabaseInterface(ConnectionHandler connection, DBNConnection conn, T entity) throws SQLException;

    @Nls
    protected abstract String getProcessDescription(T entity);

    @Nls
    protected abstract String getSuccessMessage(T entity);

    @Nls
    protected abstract String getFailureMessage(T entity);

    @Nls
    protected abstract String getProcessTitle();

    @Nls
    protected String getSuccessTitle() {
        // refrain from using key composition (would make key refactoring cumbersome)
        switch (action) {
            case CREATE: return txt("msg.objects.title.ActionSuccess_CREATE");
            case UPDATE: return txt("msg.objects.title.ActionSuccess_UPDATE");
            case DELETE: return txt("msg.objects.title.ActionSuccess_DELETE");
            case ENABLE: return txt("msg.objects.title.ActionSuccess_ENABLE");
            case DISABLE: return txt("msg.objects.title.ActionSuccess_DISABLE");
            default: return txt("msg.objects.title.ActionSuccess");
        }
    }

    @Nls
    protected  String getFailureTitle() {
        // refrain from using key composition (would make key refactoring cumbersome)
        switch (action) {
            case CREATE: return txt("msg.objects.title.ActionFailure_CREATE");
            case UPDATE: return txt("msg.objects.title.ActionFailure_UPDATE");
            case DELETE: return txt("msg.objects.title.ActionFailure_DELETE");
            case ENABLE: return txt("msg.objects.title.ActionFailure_ENABLE");
            case DISABLE: return txt("msg.objects.title.ActionFailure_DISABLE");
            default: return txt("msg.objects.title.ActionFailure");
        }
    }

}
