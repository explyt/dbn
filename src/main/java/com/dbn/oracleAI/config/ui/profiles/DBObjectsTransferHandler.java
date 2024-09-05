package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transfer handler for Database object and profile database object.
 * This handler is used durin drag and drop between tables
 */

@Slf4j
public class DBObjectsTransferHandler extends TransferHandler {

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        log.trace("DatabaseObjectsTransferHandler.canImport: drop action: " + info.getDropAction());
        // Check for String flavor
        if (!info.isDataFlavorSupported(DatabaseObjectsTransferable.DatabaseObjectFlavor) ||!info.isDrop()) {
            log.trace("DatabaseObjectsTransferHandler.canImport: -> false");
            return false;
        }

        log.trace("DatabaseObjectsTransferHandler.canImport: -> true");
        info.setShowDropLocation(true);
        return true;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        log.trace("DatabaseObjectsTransferHandler.importData");
        if (!canImport(info)) {
            return false;
        }

        JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
        int row = dl.getRow();
        JTable table = (JTable)info.getComponent();
        try {
            List<DBObjectItem> l = (List<DBObjectItem>)info.getTransferable().getTransferData(DatabaseObjectsTransferable.DatabaseObjectFlavor);
            ProfileObjectListTableModel model = ((ProfileObjectListTableModel)((JTable)info.getComponent()).getModel());
            model.addItems(l.stream().map(i->new ProfileDBObjectItem(i.getOwner(),i.getName())).collect(Collectors.toList()));
        } catch (Exception e) {
            // never happen
            log.warn("Failed to transfer data", e);
            throw new RuntimeException(e);
        }return true;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (log.isTraceEnabled()) {
            log.trace("DatabaseObjectsTransferHandler.exportDone: action == "+action+ ", data == " + data);
        }
        if (action == TransferHandler.MOVE) {
            // hide selected hitems
            DatabaseObjectsTransferable tData = (DatabaseObjectsTransferable)data;
            JTable table = (JTable)source;
            try {
                List<DBObjectItem> l = (List<DBObjectItem>)data.getTransferData(DatabaseObjectsTransferable.DatabaseObjectFlavor);
                ((DatabaseObjectListTableModel)table.getModel()).hideItemByNames(
                        l.stream().map(dbObjectItem -> dbObjectItem.getName()).collect(Collectors.toList()));

            } catch (Exception e) {
                // never happen
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        log.trace("DatabaseObjectsTransferHandler.getSourceActions -> " +  MOVE);
        return MOVE;
    }


    @Nullable
    @Override
    protected Transferable createTransferable(JComponent c) {
        log.trace("DatabaseObjectsTransferHandler.createTransferable: " + c);
        JTable table = (JTable) c;
        int[] rows = table.getSelectedRows();
        TableModel model = table.getModel();

        // TODO below code (before if check was introduced) was throwing CCE as it was always assuming the model was of type DatabaseObjectListTableModel (transfer from "selected" to "source" was always throwing CCE)
        if (model instanceof DatabaseObjectListTableModel) {
            DatabaseObjectListTableModel objectListModel = (DatabaseObjectListTableModel) table.getModel();
            List<DBObjectItem> transfered = new ArrayList<>();
            for (int row : rows) {
                transfered.add(objectListModel.getItemAt(row));
            }
            DatabaseObjectsTransferable transferable = new DatabaseObjectsTransferable(transfered);
            if (log.isTraceEnabled())
                log.trace("DatabaseObjectsTransferHandler.createTransferable new transferable: " + transferable);
            return transferable;
        }

        return null;
    }


}
