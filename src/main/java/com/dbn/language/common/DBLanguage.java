package com.dbn.language.common;

import com.dbn.code.common.style.options.DBLCodeStyleSettings;
import com.dbn.common.util.Unsafe;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.language.psql.PSQLLanguage;
import com.dbn.language.sql.SQLLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IFileElementType;
import lombok.Getter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class DBLanguage<D extends DBLanguageDialect> extends Language implements DBFileElementTypeProvider {

    private final @Getter(lazy = true) D[] languageDialects = createLanguageDialects();
    private final @Getter(lazy = true) IFileElementType fileElementType = createFileElementType();
    private final @Getter(lazy = true) SharedTokenTypeBundle sharedTokenTypes = createSharedTokenTypes();

    protected DBLanguage(final @NonNls String id, final @NonNls String... mimeTypes){
        super(id, mimeTypes);
    }

    @NotNull
    public static DBLanguage unwrap(@Nullable Language language) {
        if (language instanceof DBLanguage) return (DBLanguage) language;
        if (language instanceof DBLanguageDialect) return ((DBLanguageDialect) language).getBaseLanguage();
        return SQLLanguage.INSTANCE;
    }

    protected abstract D[] createLanguageDialects();

    protected abstract IFileElementType createFileElementType();

    private SharedTokenTypeBundle createSharedTokenTypes() {
        return new SharedTokenTypeBundle(this);
    }

    public abstract D getMainLanguageDialect();

    public D getLanguageDialect(Project project, VirtualFile virtualFile) {
        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        ConnectionHandler connection = contextManager.getConnection(virtualFile);
        if (connection != null) {
            return Unsafe.cast(connection.getLanguageDialect(this));
        }
        return getMainLanguageDialect();
    }

    public D getLanguageDialect(DBLanguageDialectIdentifier id) {
        for (D languageDialect: getLanguageDialects()) {
            if (Objects.equals(languageDialect.getID(), id.getValue())) {
                return languageDialect;
            }
        }
        return null;
    }

    public abstract DBLCodeStyleSettings codeStyleSettings(@Nullable Project project);

    public DBLanguageParserDefinition getParserDefinition(ConnectionHandler connection) {
        return connection.getLanguageDialect(this).getParserDefinition();
    }

    public static DBLanguage getLanguage(String identifier) {
        if (identifier.equalsIgnoreCase("SQL")) return SQLLanguage.INSTANCE;
        if (identifier.equalsIgnoreCase("PSQL")) return PSQLLanguage.INSTANCE;
        return null;
    }
}
