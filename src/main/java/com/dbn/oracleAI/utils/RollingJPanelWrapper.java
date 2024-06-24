package com.dbn.oracleAI.utils;

import com.dbn.oracleAI.types.AuthorType;
import com.dbn.oracleAI.ui.ChatMessage;
import com.dbn.oracleAI.ui.JIMSendTextPane;

import javax.swing.JPanel;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper class around a JPanel that will display <code>ChatMessage</code>
 * This class will maintain a fixed capacity using FIFO principle
 */
public class RollingJPanelWrapper {

    private FixedSizeList<ChatMessage> items;
    private JPanel messageContainer;
    private int maxCapacity = -1;

    /**
     * Creates a new RollingJPanelWrapper
     *
     * @param maxCapacity max capacity
     * @param panel       the panel to display the chat message
     */
    public RollingJPanelWrapper(int maxCapacity, JPanel panel) {
        this.maxCapacity = maxCapacity;
        this.messageContainer = panel;
        this.items = new FixedSizeList<>(maxCapacity);
    }

    private void ensureFreeSlot(int howMany) {
        int currentSize = items.size();
        int s = maxCapacity - currentSize - howMany;
        while (s++ < 0) {
            this.items.remove(0);
            this.messageContainer.remove(0);
        }
    }


    private JPanel createMessagePane(ChatMessage chatMessage) {
        JIMSendTextPane messagePane = new JIMSendTextPane();
        messagePane.setText(chatMessage.getMessage());
        messagePane.setAuthorColor(chatMessage.getAuthor());
        return messagePane;
    }

    public void clear() {
        this.messageContainer.removeAll();
        this.items.clear();
    }

    public void addAll(List<ChatMessage> chatMessages) {
        ensureFreeSlot(chatMessages.size());
        for (ChatMessage message : chatMessages) {
            this.items.add(message);
            JPanel messagePane = createMessagePane(message);
            this.messageContainer.add(messagePane, message.getAuthor() == AuthorType.AI ? "wrap, w ::80%" : "wrap, al right, w ::80%");
        }
        this.messageContainer.revalidate();
        this.messageContainer.repaint();
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(this.items);
    }
}
