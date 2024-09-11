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

import com.dbn.common.color.Colors;
import com.dbn.common.ui.util.Fonts;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.model.ChatMessage;
import com.dbn.oracleAI.model.ChatMessageContext;
import com.dbn.oracleAI.model.ChatMessageSection;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class AssistantChatMessageForm extends ChatMessageForm {

    public JPanel mainPanel;
    private JPanel contentPanel;
    private JLabel titleLabel;

    public AssistantChatMessageForm(ChatBoxForm parent, ChatMessage message) {
        super(parent);

        initActionToolbar(message);
        initTitlePanel(message);
        createMessagePanels(message);
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

    private void createUIComponents() {
        mainPanel = new MessagePanel();
        mainPanel.setBackground(getBackground());
    }

    private void initTitlePanel(ChatMessage message) {
        ChatMessageContext context = message.getContext();
        String title =
                context.getProfile() + " / " +
                        context.getModel() + "  -  " +
                        context.getAction().getName();

        titleLabel.setFont(Fonts.smaller(Fonts.deriveFont(Fonts.getLabelFont(), Font.BOLD), 2));
        titleLabel.setForeground(Colors.delegate(Colors::getLabelForeground));
        titleLabel.setText(title);
    }

    private void createMessagePanels(ChatMessage message) {
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        for (ChatMessageSection section : message.getSections()) {
            if (section.getLanguage() == null)
                createTextPane(section); else
                createCodePane(section);
        }
    }

    protected void createTextPane(ChatMessageSection section) {
        JTextPane textPane = new JTextPane();
        textPane.setOpaque(false);
        textPane.setEditable(false);
        textPane.setEditable(false);
        textPane.setMargin(JBUI.insets(8));
        textPane.setText(section.getContent());
        contentPanel.add(textPane);
    }

    private void createCodePane(ChatMessageSection section) {
        ChatBoxForm parent = ensureParentComponent();
        ConnectionHandler connection = parent.getConnection();

        ChatMessageCodeViewer codePanel = ChatMessageCodeViewer.create(connection, section);
        if (codePanel == null) {
            // fallback to regular text pane if code panel creation was unsuccessful
            createTextPane(section);
            return;
        }
        JPanel actionsPanel = new JPanel(new BorderLayout());
        actionsPanel.setBackground(codePanel.getViewer().getBackgroundColor());

        contentPanel.add(actionsPanel);
        contentPanel.add(codePanel);
    }


    @Override
    protected Color getBackground() {
        return Colors.delegate(() -> Colors.lafDarker(Colors.getPanelBackground(), 2));
    }

    private void initActionToolbar(ChatMessage message) {
/*
        ActionToolbar actionToolbar = Actions.createActionToolbar(actionPanel, "", false, new CopyContentAction(message.getContent()));
        JComponent component = actionToolbar.getComponent();
        component.setOpaque(false);
        component.setBorder(Borders.EMPTY_BORDER);
        actionPanel.add(component, BorderLayout.NORTH);
*/

    }
}
