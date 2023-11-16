package com.dbn.navigation.object;

import com.dbn.browser.model.BrowserTreeNode;
import com.dbn.common.dispose.StatefulDisposableBase;
import com.dbn.common.routine.AsyncTaskExecutor;
import com.dbn.common.thread.Threads;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionManager;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.list.DBObjectList;
import com.dbn.object.common.list.DBObjectListContainer;
import com.dbn.object.common.list.DBObjectListVisitor;
import com.dbn.object.type.DBObjectType;

import java.util.List;
import java.util.concurrent.TimeUnit;

class DBObjectLookupScanner extends StatefulDisposableBase implements DBObjectListVisitor {
    private final DBObjectLookupModel model;
    private final boolean forceLoad;
    private final AsyncTaskExecutor asyncScanner = new AsyncTaskExecutor(
            Threads.objectLookupExecutor(), 3, TimeUnit.SECONDS);

    DBObjectLookupScanner(DBObjectLookupModel model, boolean forceLoad) {
        this.model = model;
        this.forceLoad = forceLoad;
    }

    @Override
    public void visit(DBObjectList<?> objectList) {
        if (!isScannable(objectList)) return;

        boolean sync = objectList.isLoaded();
        if (!sync) {
            BrowserTreeNode parent = objectList.getParent();
            if (parent instanceof DBObject) {
                DBObject object = (DBObject) parent;
                if (object.getParentObject() instanceof DBSchema) {
                    sync = true;
                }
            }
        }

        if (sync) {
            doVisit(objectList);
        } else {
            asyncScanner.submit(() -> doVisit(objectList));
        }
    }

    private void doVisit(DBObjectList<?> objectList) {
        DBObjectType objectType = objectList.getObjectType();
        boolean lookupEnabled = model.isObjectLookupEnabled(objectType);
        for (DBObject object : objectList.getObjects()) {
            checkDisposed();

            if (lookupEnabled) {
                model.getData().accept(object);
            }

            object.visitChildObjects(this, true);
        }
    }

    public void scan() {
        ConnectionHandler selectedConnection = model.getSelectedConnection();
        DBSchema selectedSchema = model.getSelectedSchema();

        if (selectedConnection == null || selectedConnection.isVirtual()) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(model.getProject());
            List<ConnectionHandler> connections = connectionManager.getConnections();
            for (ConnectionHandler connection : connections) {
                model.checkCancelled();

                DBObjectListContainer objectListContainer = connection.getObjectBundle().getObjectLists();
                objectListContainer.visit(this, true);
            }
        } else {
            DBObjectListContainer objectLists =
                    selectedSchema == null ?
                            selectedConnection.getObjectBundle().getObjectLists() :
                            selectedSchema.getChildObjects();
            if (objectLists != null) {
                objectLists.visit(this, true);
            }
        }
        asyncScanner.complete();
    }

    private boolean isScannable(DBObjectList<?> objectList) {
        if (objectList == null) return false;

        DBObjectType objectType = objectList.getObjectType();
        if (!model.isListLookupEnabled(objectType)) return false;

        if (objectType.isRootObject() || objectList.isInternal()) {
            if (objectList.isLoaded()) {
                return true;
            } else {
                // todo touch?
            }
        }

        if (objectType.isSchemaObject() && objectList.getParentEntity() instanceof DBSchema) {
            if (objectList.isLoaded()) {
                return true;
            } else {
                // todo touch?
            }
        }

/*
                if (objectList.isLoaded() || objectList.canLoadFast() || forceLoad) {
                    return true;
                }
*/
        return false;
    }

    @Override
    public void checkDisposed() {
        super.checkDisposed();
        model.checkCancelled();
    }

    @Override
    public void disposeInner() {
        nullify();
    }
}
