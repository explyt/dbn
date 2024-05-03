package com.dbn.data.grid.ui.table.resultSet;

import com.dbn.common.ui.util.Mouse;
import com.dbn.common.util.Conditional;
import com.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dbn.data.grid.ui.table.basic.BasicTableGutterCellRenderer;

import javax.swing.*;
import java.awt.event.MouseListener;

import static com.dbn.common.ui.util.Mouse.isMainDoubleClick;

public class ResultSetTableGutter extends BasicTableGutter<ResultSetTable> {
    public ResultSetTableGutter(ResultSetTable table) {
        super(table);
        addMouseListener(mouseListener);
    }

    @Override
    protected ListCellRenderer<?> createCellRenderer() {
        return new BasicTableGutterCellRenderer();
    }

    MouseListener mouseListener = Mouse.listener().onClick(e ->
            Conditional.when(
                    isMainDoubleClick(e),
                    () -> getTable().showRecordViewDialog()));

    @Override
    public void disposeInner() {
        removeMouseListener(mouseListener);
        mouseListener = null;
        super.disposeInner();
    }
}
