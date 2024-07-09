package com.dbn.data.grid.ui.table.sortable;

import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.util.Cursors;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.data.grid.ui.table.basic.BasicTable;
import com.dbn.data.grid.ui.table.basic.BasicTableSpeedSearch;
import com.dbn.data.model.ColumnInfo;
import com.dbn.data.model.sortable.SortableDataModel;
import com.dbn.data.model.sortable.SortableTableHeaderMouseListener;
import com.dbn.data.sorting.SortDirection;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

public abstract class SortableTable<T extends SortableDataModel<?, ?>> extends BasicTable<T> {

    public SortableTable(DBNComponent parent, T dataModel, boolean enableSpeedSearch) {
        super(parent, dataModel);
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setDefaultRenderer(new SortableTableHeaderRenderer());
        tableHeader.addMouseListener(new SortableTableHeaderMouseListener(this));
        tableHeader.setCursor(Cursors.handCursor());

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setCellSelectionEnabled(true);
        accommodateColumnsSize();
        if (enableSpeedSearch) {
            new BasicTableSpeedSearch(this);
        }
    }

    public void sort() {
        getModel().sort();
        JTableHeader tableHeader = getTableHeader();
        UserInterface.repaint(tableHeader);
    }

    public boolean sort(int columnIndex, SortDirection sortDirection, boolean keepExisting) {
        SortableDataModel<?, ?> model = getModel();
        int modelColumnIndex = convertColumnIndexToModel(columnIndex);
        ColumnInfo columnInfo = model.getColumnInfo(modelColumnIndex);
        if (columnInfo == null) return false;
        if (!columnInfo.isSortable()) return false;
        boolean sorted = model.sort(modelColumnIndex, sortDirection, keepExisting);
        if (sorted) {
            JTableHeader tableHeader = getTableHeader();
            UserInterface.repaint(tableHeader);
        }
        return sorted;
    }

}
