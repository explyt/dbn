package com.dbn.vfs.file;

import com.dbn.connection.session.DatabaseSession;
import com.dbn.editor.DBContentType;
import com.dbn.object.DBDataset;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DBDatasetVirtualFile extends DBContentVirtualFile {
    DBDatasetVirtualFile(DBEditableObjectVirtualFile databaseFile, DBContentType contentType) {
        super(databaseFile, contentType);
    }

    @Override
    @NotNull
    public DBDataset getObject() {
        return (DBDataset) super.getObject();
    }

    @Override
    public DatabaseSession getSession() {
        return this.getConnection().getSessionBundle().getMainSession();
    }
}
