package com.dbn.editor.data.ui.table;

import com.dbn.common.ui.util.Mouse;
import com.dbn.common.util.Conditional;
import com.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dbn.editor.data.ui.table.renderer.DatasetEditorTableGutterRenderer;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static java.awt.event.MouseEvent.BUTTON1;

public class DatasetEditorTableGutter extends BasicTableGutter<DatasetEditorTable> {
    public DatasetEditorTableGutter(DatasetEditorTable table) {
        super(table);
        addMouseListener(mouseListener);
    }

    @Override
    protected ListCellRenderer<?> createCellRenderer() {
        return new DatasetEditorTableGutterRenderer();
    }

    MouseListener mouseListener = Mouse.listener().onClick(e ->
            Conditional.when(
                    e.getButton() == BUTTON1 && e.getClickCount() == 2,
                    () -> getTable().getDatasetEditor().openRecordEditor(getSelectedIndex())));

    @Override
    public void disposeInner() {
        removeMouseListener(mouseListener);
        mouseListener = null;
        super.disposeInner();
    }
}
