package com.dbn.vfs.file;

import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.ConnectionRef;
import com.dbn.connection.session.DatabaseSession;
import com.dbn.vfs.DBVirtualFileBase;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;

@Getter
@Setter
public class DBSessionBrowserVirtualFile extends DBVirtualFileBase implements Comparable<DBSessionBrowserVirtualFile> {
    private final ConnectionRef connection;
    private CharSequence content = "";

    public DBSessionBrowserVirtualFile(ConnectionHandler connection) {
        super(connection.getProject(), connection.getName() + " Sessions");
        this.connection = connection.ref();
        setCharset(connection.getSettings().getDetailSettings().getCharset());
    }

    @Override
    public Icon getIcon() {
        return Icons.FILE_SESSION_BROWSER;
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return connection.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    @Nullable
    @Override
    public DatabaseSession getSession() {
        return getConnection().getSessionBundle().getPoolSession();
    }

    @Override
    public boolean isValid() {
        return connection.isValid();
    }    

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public VirtualFile getParent() {
        ConnectionHandler connection = getConnection();
        return connection.getPsiDirectory().getVirtualFile();
    }

    @Override
    @NotNull
    public OutputStream getOutputStream(Object requestor, long modificationStamp, long timeStamp) throws IOException {
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
        return content.toString().getBytes(charset.name());
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

    @Override
    public int compareTo(@NotNull DBSessionBrowserVirtualFile o) {
        return getName().compareTo(o.getName());
    }

}
