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

package com.dbn.oracleAI.ui;

import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Actions;
import com.dbn.oracleAI.model.ChatMessage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Stub implementation for chat message forms
 * Exposes logic shared by all different chat message types (USER, AGENT, SYSTEM)
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public abstract class ChatMessageForm extends DBNFormBase {
    public ChatMessageForm(@Nullable Disposable parent) {
        super(parent);
    }

    protected abstract Color getBackground();

    protected void initActionToolbar(ChatMessage message) {
        JPanel actionPanel = getActionPanel();
        ActionToolbar actionToolbar = Actions.createActionToolbar(actionPanel, "", false, new CopyContentAction(message.getContent()));
        JComponent component = actionToolbar.getComponent();
        component.setOpaque(false);
        component.setBorder(Borders.EMPTY_BORDER);
        actionPanel.add(component, BorderLayout.NORTH);
        actionPanel.setBorder(JBUI.Borders.empty(4));
    }

    protected abstract JPanel getActionPanel();

    /**
     * Custom painted JPanel to be used as rounded-corner container for chatbox messages
     */
    static class MessagePanel extends JPanel {
        public MessagePanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            g2.dispose();
        }
    }
}
