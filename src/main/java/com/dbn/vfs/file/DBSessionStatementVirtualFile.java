package com.dbn.vfs.file;

import com.dbn.common.dispose.Failsafe;
import com.dbn.common.icon.Icons;
import com.dbn.common.ref.WeakRef;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SchemaId;
import com.dbn.connection.session.DatabaseSession;
import com.dbn.editor.session.SessionBrowser;
import com.dbn.language.common.DBLanguage;
import com.dbn.language.common.DBLanguageDialect;
import com.dbn.language.sql.SQLLanguage;
import com.dbn.vfs.DBParseableVirtualFile;
import com.dbn.vfs.DBVirtualFileBase;
import com.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;

import static com.dbn.common.action.UserDataKeys.LANGUAGE_DIALECT;

@Getter
@Setter
public class DBSessionStatementVirtualFile extends DBVirtualFileBase implements DBParseableVirtualFile {
    private final WeakRef<SessionBrowser> sessionBrowser;
    private CharSequence content;
    private SchemaId schemaId;

    public DBSessionStatementVirtualFile(SessionBrowser sessionBrowser, String content) {
        super(sessionBrowser.getProject(), sessionBrowser.getConnection().getName());
        this.sessionBrowser = WeakRef.of(sessionBrowser);
        this.content = content;
        ConnectionHandler connection = sessionBrowser.getConnection();
        setCharset(connection.getSettings().getDetailSettings().getCharset());

        DBLanguageDialect languageDialect = DBLanguageDialect.get(SQLLanguage.INSTANCE, connection);
        putUserData(LANGUAGE_DIALECT, languageDialect);
        //putUserData(PARSE_ROOT_ID_KEY, "subquery");
    }

    @Override
    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, DBLanguage<?> language) {
        ConnectionHandler connection = Failsafe.nn(getConnection());
        DBLanguageDialect languageDialect = connection.resolveLanguageDialect(language);
        return languageDialect == null ? null : fileViewProvider.initializePsiFile(languageDialect);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @NotNull
    public SessionBrowser getSessionBrowser() {
        return sessionBrowser.ensure();
    }

    @Override
    public Icon getIcon() {
        return Icons.FILE_SQL;
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return getSessionBrowser().getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        return getSessionBrowser().getConnection();
    }


    @Nullable
    @Override
    public DatabaseSession getSession() {
        return getConnection().getSessionBundle().getPoolSession();
    }

    @Override
    public boolean isWritable() {
        return true;
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
