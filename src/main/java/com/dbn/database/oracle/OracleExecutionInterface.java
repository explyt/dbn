package com.dbn.database.oracle;

import com.dbn.common.database.AuthenticationInfo;
import com.dbn.common.database.DatabaseInfo;
import com.dbn.common.util.Strings;
import com.dbn.connection.DatabaseUrlType;
import com.dbn.connection.SchemaId;
import com.dbn.database.common.execution.MethodExecutionProcessor;
import com.dbn.database.oracle.execution.OracleMethodDebugExecutionProcessor;
import com.dbn.database.oracle.execution.OracleMethodExecutionProcessor;
import com.dbn.database.CmdLineExecutionInput;
import com.dbn.database.interfaces.DatabaseExecutionInterface;
import com.dbn.execution.script.CmdLineInterface;
import com.dbn.object.DBMethod;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static java.lang.Character.isWhitespace;

@NonNls
public class OracleExecutionInterface implements DatabaseExecutionInterface {
    private static final String SQLPLUS_CONNECT_PATTERN_SID = "[USER]/[PASSWORD]@\"(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=[HOST])(Port=[PORT]))(CONNECT_DATA=(SID=[DATABASE])))\"";
    private static final String SQLPLUS_CONNECT_PATTERN_SERVICE = "[USER]/[PASSWORD]@\"(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=[HOST])(Port=[PORT]))(CONNECT_DATA=(SERVICE_NAME=[DATABASE])))\"";
    private static final String SQLPLUS_CONNECT_PATTERN_BASIC = "[USER]/[PASSWORD]@[HOST]:[PORT]/[DATABASE]";

    @Override
    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return new OracleMethodExecutionProcessor(method);
    }

    @Override
    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return new OracleMethodDebugExecutionProcessor(method);
    }

    @Override
    public CmdLineExecutionInput createScriptExecutionInput(
            @NotNull CmdLineInterface cmdLineInterface,
            @NotNull String filePath,
            String content,
            @Nullable SchemaId schemaId,
            @NotNull DatabaseInfo databaseInfo,
            @NotNull AuthenticationInfo authenticationInfo) {

        CmdLineExecutionInput executionInput = new CmdLineExecutionInput(content);
        DatabaseUrlType urlType = databaseInfo.getUrlType();
        String connectPattern =
                urlType == DatabaseUrlType.SID ? SQLPLUS_CONNECT_PATTERN_SID :
                urlType == DatabaseUrlType.SERVICE ? SQLPLUS_CONNECT_PATTERN_SERVICE :
                SQLPLUS_CONNECT_PATTERN_BASIC;

        String connectArg = connectPattern.
                replace("[USER]", authenticationInfo.getUser()).
                replace("[PASSWORD]", authenticationInfo.getPassword()).
                replace("[HOST]", databaseInfo.getHost()).
                replace("[PORT]", databaseInfo.getPort()).
                replace("[DATABASE]", databaseInfo.getDatabase());

        String fileArg = "\"@" + filePath + "\"";

        @NonNls List<String> command = executionInput.getCommand();
        command.add(cmdLineInterface.getExecutablePath());
        command.add(connectArg);
        command.add(fileArg);

        @NonNls
        StringBuilder builder = executionInput.getContent();
        if (schemaId != null) builder.insert(0, "alter session set current_schema = " + schemaId + ";\n");

        builder.insert(0, "set echo on;\n");
        builder.insert(0, "set linesize 32000;\n");
        builder.insert(0, "set pagesize 40000;\n");
        builder.insert(0, "set long 50000;\n");

        Strings.trim(builder);
        char lastChr = Strings.lastChar(builder, chr -> !isWhitespace(chr));
        if (lastChr != ';' && lastChr != '/' && lastChr != ' ') {
            // make sure exit is not impacted by script errors
            builder.append(";\n");
        }
        builder.append("\nexit;\n");
        return executionInput;
    }




}
