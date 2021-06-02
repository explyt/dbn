package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;

import javax.swing.table.TableModel;

public interface DBNTableModel<R> extends TableModel, StatefulDisposable {
    default Comparable getColumnValue(R rowObject, int column) {
        throw new UnsupportedOperationException();
    };
}
