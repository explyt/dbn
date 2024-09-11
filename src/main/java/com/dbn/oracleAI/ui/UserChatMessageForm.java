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

import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Actions;
import com.dbn.oracleAI.model.PersistentChatMessage;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

public class UserChatMessageForm extends ChatMessageForm {
    private static final Color BACKGROUND = new JBColor(new Color(218, 234, 255), new Color(68, 95, 128));

    public JPanel mainPanel;
    private JProgressBar progressBar;
    private JPanel actionPanel;
    private JTextPane messageTextPane;
    private JPanel progressPanel;

    public UserChatMessageForm(ChatBoxForm parent, PersistentChatMessage message) {
        super(parent);
        messageTextPane.setText(message.getContent());
        progressPanel.setVisible(message.isProgress());
        progressPanel.putClientProperty("CHAT_MESSAGE_PROGRESS_PANEL", true);
        progressBar.setIndeterminate(true);

        initActionToolbar(message);
    }

    private void createUIComponents() {
        mainPanel = new MessagePanel();
        mainPanel.setBackground(BACKGROUND);
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

    private void initActionToolbar(PersistentChatMessage message) {
        ActionToolbar actionToolbar = Actions.createActionToolbar(actionPanel, "", false, new CopyContentAction(message.getContent()));
        JComponent component = actionToolbar.getComponent();
        component.setOpaque(false);
        component.setBorder(Borders.EMPTY_BORDER);
        actionPanel.add(component, BorderLayout.NORTH);

    }
}
