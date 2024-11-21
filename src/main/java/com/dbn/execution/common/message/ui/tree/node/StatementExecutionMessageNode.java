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

package com.dbn.execution.common.message.ui.tree.node;

import com.dbn.common.util.Safe;
import com.dbn.execution.common.message.ui.tree.MessagesTreeLeafNode;
import com.dbn.execution.statement.StatementExecutionMessage;

public class StatementExecutionMessageNode extends MessagesTreeLeafNode<StatementExecutionMessagesFileNode, StatementExecutionMessage> {

    StatementExecutionMessageNode(StatementExecutionMessagesFileNode parent, StatementExecutionMessage executionMessage) {
        super(parent, executionMessage);
    }

    @Override
    public String toString() {
        StatementExecutionMessage executionMessage = getMessage();
        return
            executionMessage.getText() +
            Safe.call(executionMessage.getDatabaseMessage(), dm -> " " + dm.getTitle(), "") + " - Connection: " +
            executionMessage.getExecutionResult().getConnection().getName() + ": " +
            executionMessage.getExecutionResult().getExecutionDuration() + "ms";
    }
}
