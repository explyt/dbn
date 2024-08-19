package com.dbn.database.postgres;

import com.dbn.common.constant.Constants;
import com.dbn.common.database.AuthenticationInfo;
import com.dbn.common.database.DatabaseInfo;
import com.dbn.common.util.Strings;
import com.dbn.connection.AuthenticationType;
import com.dbn.connection.SchemaId;
import com.dbn.database.CmdLineExecutionInput;
import com.dbn.database.common.DatabaseExecutionInterfaceImpl;
import com.dbn.database.common.execution.MethodExecutionProcessor;
import com.dbn.database.postgres.execution.PostgresMethodExecutionProcessor;
import com.dbn.execution.script.CmdLineInterface;
import com.dbn.object.DBMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PostgresExecutionInterface extends DatabaseExecutionInterfaceImpl {
    @Override
    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return new PostgresMethodExecutionProcessor(method);
    }

    @Override
    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return null;
    }

    @Override
    public CmdLineExecutionInput createScriptExecutionInput(@NotNull CmdLineInterface cmdLineInterface, @NotNull String filePath, String content, @Nullable SchemaId schemaId, @NotNull DatabaseInfo databaseInfo, @NotNull AuthenticationInfo authenticationInfo) {
        CmdLineExecutionInput executionInput = new CmdLineExecutionInput(content);

        List<String> command = executionInput.getCommand();
        command.add(cmdLineInterface.getExecutablePath());
        command.add("--echo-all");
        command.add("--host=" + databaseInfo.getHost());

        String port = databaseInfo.getPort();
        if (Strings.isNotEmpty(port)) {
            command.add("--port=" + port);
        }

        String database = databaseInfo.getDatabase();
        if (Strings.isNotEmpty(database)) {
            command.add("--dbname=" + database);
        }

        AuthenticationType authenticationType = authenticationInfo.getType();
        if (Constants.isOneOf(authenticationType, AuthenticationType.USER, AuthenticationType.USER_PASSWORD)) {
            command.add("--username=" + authenticationInfo.getUser());
        }


        if (authenticationType != AuthenticationType.USER_PASSWORD) {
            command.add("--no-password");
        } else {
            executionInput.addEnvironmentVariable("PGPASSWORD", authenticationInfo.getPassword());
        }


        command.add("-f");
        command.add("\"" + filePath + "\"");


        //command.add("< " + filePath);

        StringBuilder contentBuilder = executionInput.getContent();
        if (schemaId != null) {
            contentBuilder.insert(0, "set search_path to " + schemaId + ";\n");
        }
        //contentBuilder.append("\nexit;\n");
        return executionInput;
    }
}