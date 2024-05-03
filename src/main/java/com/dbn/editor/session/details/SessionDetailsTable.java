package com.dbn.editor.session.details;

import com.dbn.common.icon.Icons;
import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.table.DBNTable;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Commons;
import com.dbn.common.util.Strings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class SessionDetailsTable extends DBNTable<SessionDetailsTableModel> {

    public SessionDetailsTable(@NotNull DBNComponent parent) {
        super(parent, new SessionDetailsTableModel(), false);
        setDefaultRenderer(Object.class, cellRenderer);
        setCellSelectionEnabled(true);
        adjustRowHeight(3);
    }

    private final TableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = Commons.nvl(value, "").toString();
            setText(text);
            if (column == 1 && Strings.isNotEmpty(text)) {
                switch (row) {
                    case 1: setIcon(Icons.SB_FILTER_USER); break;
                    case 2: setIcon(Icons.DBO_SCHEMA); break;
                    case 3: setIcon(Icons.SB_FILTER_SERVER); break;
                    default: setIcon(null);
                }
            } else{
                setIcon(null);
            }
            setBorder(Borders.TEXT_FIELD_INSETS);

            return component;
        }
    };
}
