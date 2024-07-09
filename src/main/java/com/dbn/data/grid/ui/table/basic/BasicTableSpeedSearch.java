package com.dbn.data.grid.ui.table.basic;

import com.dbn.common.ui.SpeedSearchBase;
import com.dbn.data.model.ColumnInfo;
import com.dbn.data.model.DataModelHeader;
import com.dbn.data.model.basic.BasicDataModel;
import lombok.Getter;

public class BasicTableSpeedSearch extends SpeedSearchBase<BasicTable<? extends BasicDataModel>> {

    @Getter(lazy = true)
    private final ColumnInfo[] columnInfos = createColumnInfos();
    private int columnIndex = 0;

    public BasicTableSpeedSearch(BasicTable<? extends BasicDataModel> table) {
        super(table);
    }

    BasicTable<? extends BasicDataModel> getTable() {
        return getComponent();
    }

    @Override
    protected int getSelectedIndex() {
        return columnIndex;
    }

    @Override
    protected Object[] getElements() {
        return getColumnInfos();
    }

    @Override
    protected String getElementText(Object o) {
        ColumnInfo columnInfo = (ColumnInfo) o;
        return columnInfo.getName();
    }

    private ColumnInfo[] createColumnInfos() {
        DataModelHeader<? extends ColumnInfo> modelHeader = getTable().getModel().getHeader();
        return modelHeader.getColumnInfos().toArray(new ColumnInfo[modelHeader.getColumnCount()]);
    }

    @Override
    protected void selectElement(Object o, String s) {
        for(ColumnInfo columnInfo : getColumnInfos()) {
            if (columnInfo != o) continue;

            columnIndex = columnInfo.getIndex();
            BasicTable table = getTable();
            int rowIndex = table.getSelectedRow();
            if (rowIndex == -1) rowIndex = 0;
            table.scrollRectToVisible(table.getCellRect(rowIndex, columnIndex, true));
            table.setColumnSelectionInterval(columnIndex, columnIndex);
            break;
        }
    }

    
}