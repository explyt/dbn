package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transfer handler for Database object and profile database object.
 * This handler is used durin drag and drop between tables
 */
public class DBObjectsTransferHandler extends TransferHandler {

    private static final Logger LOGGER = Logger.getInstance("com.dbn.oracleAI");


    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        LOGGER.trace("DatabaseObjectsTransferHandler.canImport: drop action: " + info.getDropAction());
        // Check for String flavor
        if (!info.isDataFlavorSupported(DatabaseObjectsTransferable.DatabaseObjectFlavor) ||!info.isDrop()) {
            LOGGER.trace("DatabaseObjectsTransferHandler.canImport: -> false");
            return false;
        }

        LOGGER.trace("DatabaseObjectsTransferHandler.canImport: -> true");
        info.setShowDropLocation(true);
        return true;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        LOGGER.trace("DatabaseObjectsTransferHandler.importData");
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
            LOGGER.error(e);
            throw new RuntimeException(e);
        }return true;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("DatabaseObjectsTransferHandler.exportDone: action == "+action+ ", data == " + data);
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
        LOGGER.trace("DatabaseObjectsTransferHandler.getSourceActions -> " +  MOVE);
        return MOVE;
    }


    @org.jetbrains.annotations.Nullable
    @Override
    protected Transferable createTransferable(JComponent c) {
        LOGGER.trace("DatabaseObjectsTransferHandler.createTransferable: " + c);
        JTable table = (JTable) c;
        int[] rows = table.getSelectedRows();
        DatabaseObjectListTableModel model = (DatabaseObjectListTableModel) table.getModel();
        List<DBObjectItem> transfered = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            transfered.add(model.getItemAt(rows[i]));
        }
        DatabaseObjectsTransferable transferable = new DatabaseObjectsTransferable(transfered);
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("DatabaseObjectsTransferHandler.createTransferable new transferable: " + transferable);
        return transferable;
    }


}
