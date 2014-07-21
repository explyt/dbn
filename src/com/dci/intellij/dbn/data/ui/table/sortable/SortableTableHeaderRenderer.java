package com.dci.intellij.dbn.data.ui.table.sortable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModel;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.data.sorting.SortingState;

public class SortableTableHeaderRenderer implements TableCellRenderer {
    private JPanel mainPanel;
    private JLabel nameLabel;
    private JLabel sortingLabel;

    public SortableTableHeaderRenderer() {
        mainPanel.setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        SortableDataModel model = (SortableDataModel) table.getModel();
        sortingLabel.setText(null);
        int width = 0;
        String columnName = value.toString();
        SortingState sortingState = model.getSortingState();
        SortingInstruction sortingInstruction = sortingState.getSortingInstruction(columnName);

        if (sortingInstruction != null) {
            Icon icon = sortingInstruction.getDirection() == SortDirection.ASCENDING ?
                    Icons.DATA_EDITOR_SORT_ASC :
                    Icons.DATA_EDITOR_SORT_DESC;
            sortingLabel.setIcon(icon);
            width += icon.getIconWidth();
            if (sortingState.size() > 1) {
                sortingLabel.setText(Integer.toString(sortingInstruction.getIndex()));
            }
        } else {
            sortingLabel.setIcon(null);
        }

        nameLabel.setText(columnName);

        FontMetrics fontMetrics = nameLabel.getFontMetrics(nameLabel.getFont());
        width += fontMetrics.stringWidth(columnName) + 24;
        mainPanel.setPreferredSize(new Dimension(width, (int) mainPanel.getPreferredSize().getHeight()));

        return mainPanel;
    }


}
