package com.dbn.editor.session.ui.table;

import com.dbn.common.ui.util.Mouse;
import com.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dbn.data.grid.ui.table.basic.BasicTableGutterCellRenderer;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static com.dbn.common.ui.util.Mouse.isMainDoubleClick;

public class SessionBrowserTableGutter extends BasicTableGutter<SessionBrowserTable> {
    public SessionBrowserTableGutter(SessionBrowserTable table) {
        super(table);
        addMouseListener(mouseListener);
    }

    @Override
    protected ListCellRenderer<?> createCellRenderer() {
        return new BasicTableGutterCellRenderer();
    }

    MouseListener mouseListener = Mouse.listener().onClick(e -> {
        if (isMainDoubleClick(e)) {
            // TODO
        }
    });

    @Override
    public void disposeInner() {
        removeMouseListener(mouseListener);
        mouseListener = null;
        super.disposeInner();
    }
}
