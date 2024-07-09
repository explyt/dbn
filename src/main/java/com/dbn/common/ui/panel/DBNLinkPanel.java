package com.dbn.common.ui.panel;

import com.dbn.common.ui.util.Cursors;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DBNLinkPanel extends JPanel{
    private final JLabel label;
    public DBNLinkPanel(String text) {
        label = new JLabel(text);
        label.setForeground(JBColor.BLUE);
        setCursor(Cursors.handCursor());
        label.setCursor(Cursors.handCursor());
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
    }
    
    public void setLabel(String text) {
        label.setText(text);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);
    }
}
