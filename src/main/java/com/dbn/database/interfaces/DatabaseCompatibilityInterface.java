package com.dbn.database.interfaces;

import com.dbn.connection.DatabaseAttachmentHandler;
import com.dbn.data.sorting.SortDirection;
import com.dbn.database.DatabaseFeature;
import com.dbn.database.DatabaseObjectTypeId;
import com.dbn.database.JdbcProperty;
import com.dbn.editor.session.SessionStatus;
import com.dbn.language.common.QuoteDefinition;
import com.dbn.language.common.QuotePair;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public interface DatabaseCompatibilityInterface extends DatabaseInterface {
    List<DatabaseObjectTypeId> getSupportedObjectTypes();

    List<DatabaseFeature> getSupportedFeatures();

    boolean supportsObjectType(DatabaseObjectTypeId objectTypeId);

    boolean supportsFeature(DatabaseFeature feature);

    QuoteDefinition getIdentifierQuotes();

    QuotePair getDefaultIdentifierQuotes();

    @Nullable String getDatabaseLogName();

    String getDefaultAlternativeStatementDelimiter();

    String getOrderByClause(String columnName, SortDirection sortDirection, boolean nullsFirst);

    String getForUpdateClause();

    String getSessionBrowserColumnName(String columnName);

    SessionStatus getSessionStatus(String statusName);

    @NonNls
    String getExplainPlanStatementPrefix();

    @Nullable DatabaseAttachmentHandler getDatabaseAttachmentHandler();

    <T> T attemptFeatureInvocation(JdbcProperty feature, Callable<T> invoker) throws SQLException;
}
