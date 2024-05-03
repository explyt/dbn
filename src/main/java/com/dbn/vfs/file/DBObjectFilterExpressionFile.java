package com.dbn.vfs.file;

import com.dbn.common.dispose.Failsafe;
import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.language.common.DBLanguageDialect;
import com.dbn.language.sql.SQLLanguage;
import com.dbn.object.filter.custom.ObjectFilter;
import com.dbn.vfs.DBParseableVirtualFile;
import com.dbn.vfs.DBVirtualFileBase;
import com.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;

import static com.dbn.common.action.UserDataKeys.LANGUAGE_DIALECT;

@Getter
@Setter
public class DBObjectFilterExpressionFile extends DBVirtualFileBase implements DBParseableVirtualFile {
    private ObjectFilter<?> filter;
    private CharSequence content;

    public DBObjectFilterExpressionFile(ObjectFilter<?> filter) {
        super(filter.getProject(), filter.getObjectType().getName());
        this.filter = filter;
        this.content = filter.getExpression();

        ConnectionHandler connection = getConnection();
        Charset charset = connection.getSettings().getDetailSettings().getCharset();
        setCharset(charset);
        putUserData(PARSE_ROOT_ID_KEY, "condition");
        putUserData(LANGUAGE_DIALECT, DBLanguageDialect.get(SQLLanguage.INSTANCE, connection));
    }

    @Override
    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connection = Failsafe.nn(getConnection());
        DBLanguageDialect languageDialect = connection.resolveLanguageDialect(language);
        return languageDialect == null ? null : fileViewProvider.initializePsiFile(languageDialect);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Icon getIcon() {
        return Icons.DATASET_FILTER_BASIC;
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        return filter.getConnection();
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return getConnection().getConnectionId();
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

    @Override
    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
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
