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

package com.dbn.assistant.chat.window.util;

import com.dbn.assistant.chat.message.PersistentChatMessage;
import com.dbn.assistant.chat.message.ui.ChatMessageForm;
import com.dbn.assistant.chat.message.ui.ChatMessagePanel;
import com.dbn.assistant.chat.window.ui.ChatBoxForm;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class around a JPanel that will display <code>ChatMessage</code>
 * This class will maintain a fixed capacity using FIFO principle
 *
 * @author Emmanuel Jannetti (Oracle)
 */
@Slf4j
public class RollingJPanelWrapper {

  private final ConnectionRef connection;
  private final FixedSizeList<PersistentChatMessage> items;
  private final JPanel messageContainer;
  private int maxCapacity = -1;

  /**
   * Creates a new RollingJPanelWrapper
   *
   * @param maxCapacity max capacity
   * @param panel       the panel to display the chat message
   *
   * @author Emmanuel Jannetti (Oracle)
   */
  public RollingJPanelWrapper(ConnectionHandler connection, int maxCapacity, JPanel panel) {
    this.connection = ConnectionRef.of(connection);
    this.maxCapacity = maxCapacity;
    this.messageContainer = panel;
    this.items = new FixedSizeList<>(maxCapacity);
    this.messageContainer.setLayout(new MigLayout("fillx"));
  }


  private ConnectionHandler getConnection() {
    return connection.ensure();
  }

  private void ensureFreeSlot(int howMany) {
    int currentSize = items.size();
    int s = maxCapacity - currentSize - howMany;
    while (s++ < 0) {
      this.items.remove(0);
      this.messageContainer.remove(0);
    }
  }

  private void removeProgressIndicator() {
    Component[] messagePanels = messageContainer.getComponents();
    if (messagePanels.length == 0) return;

    Component panel = messagePanels[messagePanels.length - 1];
    if (panel instanceof JComponent) {
      // identify the message panels that have progress indicators and hide them
      JComponent component = (JComponent) panel;
      UserInterface.visitRecursively(component, JProgressBar.class, b -> b.setVisible(false));
    }
  }

  public void clear() {
    this.messageContainer.removeAll();
    this.items.clear();
    UserInterface.repaint(messageContainer);
  }

  public void addAll(List<PersistentChatMessage> chatMessages, ChatBoxForm parent) {
    removeProgressIndicator();
    ensureFreeSlot(chatMessages.size());

    for (PersistentChatMessage message : chatMessages) {
      this.items.add(message);
      JComponent messagePane;
      ChatMessageForm form = ChatMessageForm.create(parent, message);

      // TODO remove fallback on old CHatMessagePanel when all author types are handled
      messagePane = form == null ? new ChatMessagePanel(getConnection(), message) : form.getComponent();

      this.messageContainer.add(messagePane, "growx, wrap, w ::93%"); // TODO try to occupy the entire width (100% breaks the wrapping for some reason)
    }
    UserInterface.repaint(messageContainer);
  }

  public List<PersistentChatMessage> getMessages() {
    return new ArrayList<>(this.items);
  }
}
