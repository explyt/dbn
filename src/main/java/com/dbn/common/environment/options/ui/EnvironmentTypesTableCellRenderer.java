package com.dbn.common.environment.options.ui;

import com.dbn.common.color.Colors;
import com.dbn.common.ui.table.DBNColoredTableCellRenderer;
import com.dbn.common.ui.table.DBNTable;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Strings;
import com.dbn.common.environment.EnvironmentType;
import com.intellij.ui.ColoredSideBorder;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class EnvironmentTypesTableCellRenderer extends DBNColoredTableCellRenderer {

    public static final Border BORDER = Borders.insetBorder(2, 1, 1, 1);

    @Override
    protected void customizeCellRenderer(DBNTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        if (column == 2 || column == 3) {

        }
        else if (column == 4) {
            Color color = (Color) value;
            if (color == null) color = EnvironmentType.EnvironmentColor.NONE;
            setBackground(color);
/*
            setBorder(new CompoundBorder(
                    new LineBorder(table.getBackground()),
                    new ColoredSideBorder(color.brighter(), color.brighter(), color.darker(), color.darker(), 1)));
*/
            setBorder(Borders.lineBorder(Colors.getTableBackground(), 3));
        } else {
            String stringValue = (String) value;
            if (Strings.isNotEmpty(stringValue)) {
                append(stringValue, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }

            setBorder(BORDER);
        }


    }
}
