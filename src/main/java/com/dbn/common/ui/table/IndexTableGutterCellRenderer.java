package com.dbn.common.ui.table;

import com.dbn.common.color.Colors;

import javax.swing.JList;

public class IndexTableGutterCellRenderer extends DBNTableGutterRendererBase {


    @Override
    protected void adjustListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DBNTableGutter tableGutter = (DBNTableGutter) list;
        DBNTable table = tableGutter.getTable();
        boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == index && table.getSelectedRowCount() == 1;

        mainPanel.setBackground(isSelected ?
                Colors.getTableSelectionBackground(true) :
                isCaretRow ?
                        Colors.getTableCaretRowColor() :
                        table.getBackground());
        textLabel.setForeground(isSelected ?
                Colors.getTableSelectionForeground(cellHasFocus) :
                Colors.getTableGutterForeground());
    }
}
