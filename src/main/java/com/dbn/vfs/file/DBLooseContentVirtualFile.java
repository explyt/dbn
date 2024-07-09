package com.dbn.vfs.file;

import com.dbn.common.dispose.Failsafe;
import com.dbn.common.util.SlowOps;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SchemaId;
import com.dbn.connection.session.DatabaseSession;
import com.dbn.language.common.DBLanguage;
import com.dbn.language.common.DBLanguageDialect;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.lookup.DBObjectRef;
import com.dbn.vfs.DBParseableVirtualFile;
import com.dbn.vfs.DBVirtualFileBase;
import com.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;

@Getter
@Setter
public class DBLooseContentVirtualFile extends DBVirtualFileBase implements DBParseableVirtualFile {
    private final DBObjectRef<DBSchemaObject> object;
    private final FileType fileType;
    private CharSequence content;

    public DBLooseContentVirtualFile(DBSchemaObject object, String content, FileType fileType) {
        super(object.getProject(), object.getName());
        this.object = DBObjectRef.of(object);
        this.content = content;
        this.fileType = fileType;
        ConnectionHandler connection = Failsafe.nn(getConnection());
        setCharset(connection.getSettings().getDetailSettings().getCharset());
    }

    @Override
    public boolean isValid() {
        return SlowOps.isValid(object);
    }

    @Override
    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, DBLanguage<?> language) {
        ConnectionHandler connection = Failsafe.nn(getConnection());
        DBLanguageDialect languageDialect = connection.resolveLanguageDialect(language);
        return languageDialect == null ? null : fileViewProvider.initializePsiFile(languageDialect);
    }

    @NotNull
    public DBObject getObject() {
        return DBObjectRef.ensure(object);
    }

    @Override
    public Icon getIcon() {
        return object.getObjectType().getIcon();
    }


    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return getObject().getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        return getObject().ensureConnection();
    }

    @Nullable
    @Override
    public SchemaId getSchemaId() {
        return getObject().getSchemaId();
    }

    @Nullable
    @Override
    public DatabaseSession getSession() {
        return getConnection().getSessionBundle().getMainSession();
    }

    @Override
    @NotNull
    public OutputStream getOutputStream(Object requestor, final long modificationStamp, long timeStamp) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                setContent(this.toString());

                setTimeStamp(timeStamp);
                setModificationStamp(modificationStamp);
            }
        };
    }

    @Override
    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        Charset charset = getCharset();
        return content.toString().getBytes(charset);
    }

    @Override
    public long getLength() {
        return content.length();
    }

    @NotNull
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(contentsToByteArray());
    }

    @Override
    public String getExtension() {
        return "sql";
    }

}
