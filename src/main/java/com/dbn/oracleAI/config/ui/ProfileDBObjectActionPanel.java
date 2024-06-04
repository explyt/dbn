package com.dbn.oracleAI.config.ui;

import com.dbn.common.icon.Icons;
import lombok.Getter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

@Getter
/**
 * Custom action panel used in profile DB object list
 */
class ProfileDBObjectActionPanel {
    private JPanel panel;
    private JButton addButton = null;
    private JButton removeButton = null;

    public enum TYPE {
        ADD, // add add button
        ADD_REMOVE // add add and remove button
    }

    private class CustomButton extends JButton {
        public CustomButton(Icon icon) {
            super(icon);
            setBorder(BorderFactory.createEmptyBorder());
            setEnabled(true);
            setOpaque(false);
            setMaximumSize(new Dimension(10, 10));
            setPreferredSize(new Dimension(10, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Creates a new panel
     * @param cellValue the value to be dispayed in the panel label
     * @param type type of action to be added to the panel
     */
    public ProfileDBObjectActionPanel(String cellValue,TYPE type) {
        panel = new JPanel();
        panel.setBackground(Color.WHITE);
        BorderLayout bl = new BorderLayout(0, 0);

        panel.setLayout(bl);

        panel.add(new JLabel(cellValue), BorderLayout.CENTER);

        addButton = new CustomButton(Icons.ACTION_ADD);

        if (type.equals(TYPE.ADD_REMOVE))
            removeButton = new CustomButton(Icons.ACTION_REMOVE);

        JPanel toolbar = new JPanel();
        toolbar.setOpaque(false);
        BoxLayout bol = new BoxLayout(toolbar, BoxLayout.X_AXIS);

        toolbar.setLayout(bol);
        toolbar.add(addButton);
        if (type.equals(TYPE.ADD_REMOVE))
           toolbar.add(removeButton);

        panel.add(toolbar, BorderLayout.EAST);

    }
}
