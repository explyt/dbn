package com.dbn.data.grid.ui.table.resultSet;

import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.util.Borderless;
import com.dbn.common.ui.util.Mouse;
import com.dbn.common.util.Dialogs;
import com.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dbn.data.grid.ui.table.resultSet.record.ResultSetRecordViewerDialog;
import com.dbn.data.grid.ui.table.sortable.SortableTable;
import com.dbn.data.model.resultSet.ResultSetDataModel;
import com.dbn.data.record.RecordViewInfo;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import static com.dbn.common.ui.util.Mouse.isMainDoubleClick;
import static com.dbn.common.util.Conditional.when;

@Getter
public class ResultSetTable<T extends ResultSetDataModel<?, ?>> extends SortableTable<T> implements Borderless {
    private final RecordViewInfo recordViewInfo;

    public ResultSetTable(DBNComponent parent, T dataModel, boolean enableSpeedSearch, RecordViewInfo recordViewInfo) {
        super(parent, dataModel, enableSpeedSearch);
        this.recordViewInfo = recordViewInfo;
        addMouseListener(Mouse.listener().onClick(
                e -> when(isMainDoubleClick(e),
                () -> showRecordViewDialog())));
    }

    public void showRecordViewDialog() {
        Dialogs.show(() -> new ResultSetRecordViewerDialog(this, showRecordViewDataTypes()));
    }

    @Override
    protected BasicTableGutter<?> createTableGutter() {
        return new ResultSetTableGutter(this);
    }

    protected boolean showRecordViewDataTypes() {
        return true;
    }

    @NotNull
    @Override
    public T getModel() {
        return super.getModel();
    }


    public void hideColumn(int columnIndex) {
        checkColumnBounds(columnIndex);

        TableColumnModel columnModel = getColumnModel();
        int viewColumnIndex = convertColumnIndexToView(columnIndex);
        checkColumnBounds(viewColumnIndex);

        TableColumn column = columnModel.getColumn(viewColumnIndex);
        columnModel.removeColumn(column);
    }

    public void hideAuditColumns() {
        // TODO
    }

    public void showAuditColumns() {
        // TODO
    }
}
