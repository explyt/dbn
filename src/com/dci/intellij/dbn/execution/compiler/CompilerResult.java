package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class CompilerResult implements Disposable {
    private Project project;
    private DBObjectRef<DBSchemaObject> objectRef;
    private List<CompilerMessage> compilerMessages = new ArrayList<CompilerMessage>();
    private boolean isError = false;

    public CompilerResult(DBSchemaObject object) {
        objectRef = DBObjectRef.from(object);
        project = object.getProject();
        Connection connection = null;
        ResultSet resultSet = null;
        List<CompilerMessage> echoMessages = new ArrayList<CompilerMessage>();
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        try {
            connection = connectionHandler.getPoolConnection();
            resultSet = connectionHandler.getInterfaceProvider().getMetadataInterface().loadCompileObjectErrors(
                    object.getSchema().getName(),
                    object.getName(),
                    connection);

            while (resultSet != null && resultSet.next()) {
                CompilerMessage errorMessage = new CompilerMessage(this, resultSet);
                isError = true;
                if (errorMessage.isEcho()) {
                    echoMessages.add(errorMessage);
                } else {
                    compilerMessages.add(errorMessage);
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            connectionHandler.freePoolConnection(connection);
            ConnectionUtil.closeResultSet(resultSet);
        }

        if (compilerMessages.size() == 0) {
            if (echoMessages.size() > 0) {
                compilerMessages.addAll(echoMessages);
                isError = true;
            } else {
                CompilerMessage compilerMessage = new CompilerMessage(this, "The " + object.getQualifiedNameWithType() + " was updated successfully.");
                compilerMessages.add(compilerMessage);
            }
        }
    }

    public boolean isError() {
        return isError;
    }

    public CompilerResult(DBSchemaObject object, String errorMessage) {
        objectRef = DBObjectRef.from(object);
        CompilerMessage compilerMessage = new CompilerMessage(this, errorMessage, MessageType.ERROR);
        compilerMessages.add(compilerMessage);
    }

    public List<CompilerMessage> getCompilerMessages() {
        return compilerMessages;
    }

    @Nullable
    public DBSchemaObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    public Project getProject() {
        return project;
    }

    public void dispose() {
        compilerMessages.clear();
        objectRef = null;
        project = null;
    }
}
