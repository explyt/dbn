package com.dbn.vfs.file;

import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ConnectionSettings;
import com.dbn.language.common.DBLanguage;
import com.dbn.language.common.DBLanguageDialect;
import com.dbn.language.sql.SQLLanguage;
import com.dbn.object.filter.custom.ObjectFilter;
import com.dbn.vfs.DBParseableVirtualFile;
import com.dbn.vfs.DBVirtualFileBase;
import com.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;

import static com.dbn.common.action.UserDataKeys.LANGUAGE_DIALECT;
import static com.dbn.common.dispose.Failsafe.nd;

@Getter
@Setter
public class DBObjectFilterExpressionFile extends DBVirtualFileBase implements DBParseableVirtualFile {
    private ObjectFilter<?> filter;
    private CharSequence content;

    public DBObjectFilterExpressionFile(ObjectFilter<?> filter) {
        super(filter.getProject(), filter.getObjectType().getName());
        this.filter = filter;
        this.content = filter.getExpression();

        Charset charset = resolveCharset();
        DBLanguageDialect languageDialect = getLanguageDialect();

        setCharset(charset);
        putUserData(PARSE_ROOT_ID_KEY, "condition");
        putUserData(LANGUAGE_DIALECT, languageDialect);
    }

    private Charset resolveCharset() {
        return getConnectionSettings().getDetailSettings().getCharset();
    }

    private @NotNull ConnectionSettings getConnectionSettings() {
        return nd(filter.getSettings().getParentOfType(ConnectionSettings.class));
    }

    @Override
    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, DBLanguage<?> language) {
        DBLanguageDialect languageDialect = getLanguageDialect();
        return languageDialect == null ? null : fileViewProvider.initializePsiFile(languageDialect);
    }

    private DBLanguageDialect getLanguageDialect() {
        ConnectionSettings connectionSettings = getConnectionSettings();
        return connectionSettings.getLanguageDialect(SQLLanguage.INSTANCE);
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
        return filter.getConnectionId();
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
