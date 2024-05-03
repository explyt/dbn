package com.dbn.common.ui.table;

import com.dbn.common.color.Colors;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.ui.util.Mouse;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ActionsToolbarTableCellRenderer implements TableCellRenderer {
    private final JPanel mainPanel = new JPanel();

    public ActionsToolbarTableCellRenderer() {
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mainPanel.setBackground(Colors.getTableBackground());
    }

    public ActionsToolbarTableCellRenderer withAction(Icon icon, Runnable action) {
        JLabel actionLabel = new JLabel(icon);
        actionLabel.setBorder(Borders.lineBorder(Colors.getTableBackground(), 1));
        actionLabel.addMouseListener(Mouse.listener().onClick(e -> action.run()));
        mainPanel.add(actionLabel);
        return this;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return mainPanel;
    }
}
