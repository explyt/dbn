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

package com.dbn.object.event;

import com.dbn.common.Priority;
import com.dbn.common.component.ConnectionComponent;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.outcome.Outcome;
import com.dbn.common.outcome.OutcomeHandler;
import com.dbn.connection.ConnectionHandler;
import com.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

/**
 * Generic implementation of an {@link OutcomeHandler} that sends out a project notification of type {@link ObjectChangeListener}
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class ObjectChangeNotifier extends ConnectionComponent implements OutcomeHandler {
    private final DBObjectType objectType;
    private final ObjectChangeAction action;

    private ObjectChangeNotifier(@NotNull ConnectionHandler connection, DBObjectType objectType, ObjectChangeAction action) {
        super(connection);
        this.objectType = objectType;
        this.action = action;
    }

    public static OutcomeHandler create(ConnectionHandler connection, DBObjectType objectType, ObjectChangeAction action) {
        return new ObjectChangeNotifier(connection, objectType, action);
    }

    @Override
    public void handle(Outcome outcome) {
        ProjectEvents.notify(getProject(), ObjectChangeListener.TOPIC, l -> notify(l));
    }

    private void notify(ObjectChangeListener listener) {
        listener.objectsChanged(getConnectionId(), null, objectType, action);
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }
}
