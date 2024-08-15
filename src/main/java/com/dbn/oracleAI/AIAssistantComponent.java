package com.dbn.oracleAI;

import com.dbn.common.component.ConnectionComponent;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.interfaces.DatabaseOracleAIInterface;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public abstract class AIAssistantComponent extends ConnectionComponent {
    public AIAssistantComponent(@NotNull ConnectionHandler connection) {
        super(connection);
    }

    protected final DatabaseOracleAIInterface getAssistantInterface() {
        return getConnection().getOracleAIInterface();
    }

    protected final DBNConnection getAssistantConnection() throws SQLException {
        return getConnection(SessionId.ORACLE_AI);
    }
}
