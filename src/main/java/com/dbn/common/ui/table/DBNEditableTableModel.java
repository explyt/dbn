package com.dbn.common.ui.table;

import com.dbn.common.dispose.StatefulDisposableBase;
import com.dbn.common.latent.Latent;
import com.dbn.common.ui.util.Listeners;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public abstract class DBNEditableTableModel extends StatefulDisposableBase implements DBNTableWithGutterModel {
    private final Listeners<TableModelListener> listeners = Listeners.create(this);
    private final Latent<DBNTableGutterModel<?>> listModel = Latent.basic(() -> new DBNTableGutterModel<>(DBNEditableTableModel.this));

    @Override
    public void addTableModelListener(TableModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTableModelListener(TableModelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public ListModel<?> getListModel() {
        return listModel.get();
    }

    public abstract void insertRow(int rowIndex);

    public abstract void removeRow(int rowIndex);

    public void notifyListeners(int firstRowIndex, int lastRowIndex, int columnIndex) {
        TableModelEvent modelEvent = new TableModelEvent(this, firstRowIndex, lastRowIndex, columnIndex);
        listeners.notify(l -> l.tableChanged(modelEvent));

        if (listModel.loaded()) {
            ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, firstRowIndex, lastRowIndex);
            listModel.get().notifyListeners(listDataEvent);
        }
    }

    @Override
    public void disposeInner() {
        nullify();
    }
}
