package com.dbn.data.export.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.ref.WeakRef;
import com.dbn.common.util.Dialogs;
import com.dbn.data.export.ui.ExportDataDialog;
import com.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dbn.object.DBDataset;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ExportDataAction extends BasicAction {
    private final WeakRef<ResultSetTable<?>> table;
    private final DBObjectRef<DBDataset> dataset;

    public ExportDataAction(ResultSetTable<?> table, DBDataset dataset) {
        super("Export Data", null, Icons.DATA_EXPORT);
        this.table = WeakRef.of(table);
        this.dataset = DBObjectRef.of(dataset);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Dialogs.show(() -> new ExportDataDialog(table.ensure(), dataset.ensure()));
    }
}
