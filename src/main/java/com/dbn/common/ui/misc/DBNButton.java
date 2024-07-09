package com.dbn.common.ui.misc;

import com.dbn.common.ui.util.Borders;
import com.dbn.common.ui.util.Cursors;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DBNButton extends JLabel {
    public DBNButton(Icon image) {
        super(image);
        setBorder(Borders.buttonBorder());
        setCursor(Cursors.handCursor());
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
    }

    @Override
    public void addMouseListener(MouseListener l) {
        super.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isEnabled()) l.mouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) l.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) l.mouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) l.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) l.mouseEntered(e);
            }
        });
    }

    @Override
    public void addKeyListener(KeyListener l) {
        super.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (isEnabled()) l.keyTyped(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (isEnabled()) l.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (isEnabled()) l.keyReleased(e);
            }
        });
    }
}
