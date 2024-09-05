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

package com.dbn.oracleAI.action;

import com.dbn.common.action.DataKeys;
import com.dbn.common.action.ToggleAction;
import com.dbn.oracleAI.types.ActionAIType;
import com.dbn.oracleAI.ui.ChatBoxForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Action for selecting the type of interaction with the AI-assistant engine
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class TypeSelectAction extends ToggleAction {
    private final ActionAIType type;

    TypeSelectAction(ActionAIType type) {
        super(type.getName(), type.getDescription(), null);
        this.type = type;
        getTemplatePresentation().setIcon(null);
    }

    @Override
    public boolean displayTextInToolbar() {
        return true;
    }

    public static class ShowSQL extends TypeSelectAction {
        public ShowSQL() {
            super(ActionAIType.SHOW_SQL);
        }
    }

    public static class ExplainSQL extends TypeSelectAction {
        public ExplainSQL() {
            super(ActionAIType.EXPLAIN_SQL);
        }
    }

    public static class Narrate extends TypeSelectAction {
        public Narrate() {
            super(ActionAIType.NARRATE);
        }
    }

    public static class Chat extends TypeSelectAction {
        public Chat() {
            super(ActionAIType.CHAT);
        }
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        ChatBoxForm chatBox = e.getData(DataKeys.ASSISTANT_CHAT_BOX);
        if (chatBox != null) return chatBox.getState().getSelectedAction() == type;
        return false;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        if(!state) return;
        ChatBoxForm chatBox = e.getData(DataKeys.ASSISTANT_CHAT_BOX);
        if (chatBox == null) return;

        chatBox.selectAction(type);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        ChatBoxForm chatBox = e.getData(DataKeys.ASSISTANT_CHAT_BOX);
        boolean enabled = chatBox != null && chatBox.getState().promptingAvailable();
        e.getPresentation().setEnabled(enabled);
    }
}
