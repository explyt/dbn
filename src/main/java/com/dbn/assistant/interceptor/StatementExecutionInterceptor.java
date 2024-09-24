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

package com.dbn.assistant.interceptor;

import com.dbn.assistant.DatabaseAssistantManager;
import com.dbn.assistant.entity.AIProfileItem;
import com.dbn.common.interceptor.Interceptor;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.execution.statement.StatementExecutionContext;
import com.dbn.execution.statement.StatementExecutionInput;
import com.dbn.language.common.element.ElementType;
import com.dbn.language.common.element.util.ElementTypeAttribute;
import com.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.project.Project;
import lombok.SneakyThrows;

public class StatementExecutionInterceptor implements Interceptor<StatementExecutionContext> {
    public static final StatementExecutionInterceptor INSTANCE = new StatementExecutionInterceptor();

    private StatementExecutionInterceptor() {}

    @Override
    public boolean supports(StatementExecutionContext context) {
        StatementExecutionInput input = context.getInput();
        ExecutablePsiElement element = input.getExecutablePsiElement();
        if (element == null) return false;

        ElementType specificElementType = element.getSpecificElementType(true);
        return specificElementType.is(ElementTypeAttribute.DB_ASSISTANT);
    }

    @Override
    @SneakyThrows
    public void before(StatementExecutionContext context) {
        StatementExecutionInput input = context.getInput();
        ConnectionHandler connection = input.getConnection();
        if (connection == null) return;

        DBNConnection conn = context.getConnection();
        if (conn == null) return;

        Project project = context.getProject();
        DatabaseAssistantManager assistantManager = DatabaseAssistantManager.getInstance(project);
        AIProfileItem profile = assistantManager.getDefaultProfile(connection.getConnectionId());
        if (profile == null) return;

        connection.getAssistantInterface().setCurrentProfile(conn, profile.getName());
    }
}
