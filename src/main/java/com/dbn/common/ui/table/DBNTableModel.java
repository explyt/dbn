package com.dbn.common.ui.table;

import com.dbn.common.dispose.StatefulDisposable;
import com.dbn.common.exception.OutdatedContentException;
import com.dbn.nls.NlsSupport;

import javax.swing.table.TableModel;

public interface DBNTableModel<R> extends TableModel, StatefulDisposable, NlsSupport {
    default String getPresentableValue(R rowObject, int column) {
        return rowObject == null ? "" : rowObject.toString();
    };

    default Object getValue(R rowObject, int column) {
        throw new UnsupportedOperationException();
    };

    default void checkRowBounds(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount()) throw new OutdatedContentException(this);
    }

    default void checkColumnBounds(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= getColumnCount()) throw new OutdatedContentException(this);
    }
}
