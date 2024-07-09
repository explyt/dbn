package com.dbn.data.grid.ui.table.basic;

import com.dbn.common.color.Colors;
import com.dbn.common.ui.table.DBNTable;
import com.dbn.common.ui.table.DBNTableGutterRendererBase;

import javax.swing.*;

import static com.dbn.common.dispose.Checks.isValid;

public class BasicTableGutterCellRenderer extends DBNTableGutterRendererBase {

    @Override
    protected void adjustListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        BasicTableGutter tableGutter = (BasicTableGutter) list;

        DBNTable table = tableGutter.getTable();
        boolean isCaretRow = isValid(table) &&
                table.getCellSelectionEnabled() &&
                table.getSelectedRow() == index &&
                table.getSelectedRowCount() == 1;

        mainPanel.setBackground(isSelected ?
                Colors.getTableSelectionBackground(cellHasFocus) :
                isCaretRow ?
                        Colors.getTableCaretRowColor() :
                        table.getBackground());
        textLabel.setForeground(isSelected ?
                Colors.getTableSelectionForeground(cellHasFocus) :
                Colors.getTableGutterForeground());
    }
}
