package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
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


    @org.jetbrains.annotations.Nullable
    @Override
    protected Transferable createTransferable(JComponent c) {
        log.trace("DatabaseObjectsTransferHandler.createTransferable: " + c);
        JTable table = (JTable) c;
        int[] rows = table.getSelectedRows();
        DatabaseObjectListTableModel model = (DatabaseObjectListTableModel) table.getModel();
        List<DBObjectItem> transfered = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            transfered.add(model.getItemAt(rows[i]));
        }
        DatabaseObjectsTransferable transferable = new DatabaseObjectsTransferable(transfered);
        if (log.isTraceEnabled())
            log.trace("DatabaseObjectsTransferHandler.createTransferable new transferable: " + transferable);
        return transferable;
    }


}
