/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.dbn.database.common.execution;

import com.dbn.common.thread.CancellableDatabaseCall;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.Resources;
import com.dbn.connection.SchemaId;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.connection.jdbc.DBNPreparedStatement;
import com.dbn.debugger.DBDebuggerType;
import com.dbn.execution.ExecutionOption;
import com.dbn.execution.ExecutionOptions;
import com.dbn.execution.ExecutionStatus;
import com.dbn.execution.java.JavaExecutionContext;
import com.dbn.execution.java.JavaExecutionInput;
import com.dbn.execution.logging.DatabaseLoggingManager;
import com.dbn.execution.java.result.JavaExecutionResult;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBJavaParameter;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.dbn.common.dispose.Failsafe.nd;
import static com.dbn.common.dispose.Failsafe.nn;
import static com.dbn.database.oracle.execution.OracleJavaExecutionProcessor.dataTypeMap;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public abstract class JavaExecutionProcessorImpl implements JavaExecutionProcessor {
	private final DBObjectRef<DBJavaMethod> method;

	private static final Set<String> parameterNames = new HashSet<>();
	protected JavaExecutionProcessorImpl(DBJavaMethod method) {
		this.method = DBObjectRef.of(method);
	}

	@Override
	@NotNull
	public DBJavaMethod getMethod() {
		return DBObjectRef.ensure(method);
	}

	public List<DBJavaParameter> getArguments() {
		DBJavaMethod method = getMethod();
		List<DBJavaParameter> parameter = method.getParameters();
		List<DBJavaParameter> reverseParameter = new ArrayList<>();
		for(DBJavaParameter param:parameter){
			reverseParameter.add(0,param);
		}
		return reverseParameter;

	}

	protected int getArgumentsCount() {
		return getArguments().size();
	}

	protected String getReturnArgument() {
		DBJavaMethod method = getMethod();
		return method.getReturnType();
	}


	@Override
	public void execute(JavaExecutionInput executionInput, DBDebuggerType debuggerType) throws SQLException {
		ConnectionHandler connection = getConnection();
		SessionId targetSessionId = executionInput.getTargetSessionId();
		SchemaId targetSchemaId = executionInput.getTargetSchemaId();
		DBNConnection conn = connection.getConnection(targetSessionId, targetSchemaId);

		if (targetSessionId == SessionId.POOL) {
			Resources.setAutoCommit(conn, false);
		}

		execute(executionInput, conn, debuggerType);
	}

	@Override
	public void execute(JavaExecutionInput executionInput, @NotNull DBNConnection conn, DBDebuggerType debuggerType) throws SQLException {
		JavaExecutionContext context = executionInput.initExecution(debuggerType);
		context.setConnection(conn);
		context.setDebuggerType(debuggerType);
		context.set(ExecutionStatus.EXECUTING, true);

		try {
			// create java wrapper
			initCreateWrapperCommand(context);
			initTimeout(context);
			execute(context);

			// call java wrapper
			initCommand(context);
			initLogging(context);
			initTimeout(context);
			if(isQuery())
				initParameters(context);
			execute(context);

			// drop java wrapper
			initDropWrapperCommand(context);
			initTimeout(context);
			execute(context);
		} catch (SQLException e) {
			conditionallyLog(e);
			Resources.cancel(context.getStatement());
			throw e;
		} finally {
			release(context);
			parameterNames.clear();
		}
	}

	private void release(JavaExecutionContext context) {
		ConnectionHandler connection = nn(context.getTargetConnection());
		DBNConnection conn = context.getConnection();
		if (context.isLogging()) {
			DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(getProject());
			loggingManager.disableLogger(connection, conn);
		}

		ExecutionOptions options = context.getOptions();
		if (options.is(ExecutionOption.COMMIT_AFTER_EXECUTION)) {
			Resources.commitSilently(conn);
		}

		if (conn.isDebugConnection()) {
			Resources.close(conn);

		} else if (conn.isPoolConnection()) {
			connection.freePoolConnection(conn);
		}
	}

	private static void appendVariableName(StringBuilder buffer, String argument) {
		String variableName = "var_" + argument;

		// if a method has repeated data type ( int, int, int ), then name it as ( var_int, var_int1, var_int2 )
		int index = 1;
		while(parameterNames.contains(variableName)){
			variableName = "var_" + argument + index;
			index++;
		}
		parameterNames.add(variableName);
		buffer.append(variableName);
	}

	private static void appendSQLType(StringBuilder buffer, String javaType){
		buffer.append(" ").append(dataTypeMap.get(javaType));
	}

	private void initCreateWrapperCommand(JavaExecutionContext context) throws SQLException{
		DBNConnection conn = context.getConnection();

		DBJavaMethod javaMethod = getMethod();
		String wrapperName = javaMethod.getName().split("#")[0] + "_wrapper";

		String returnArgument = getReturnArgument();
		List<DBJavaParameter> arguments = getArguments();

		boolean isProcedure = returnArgument.equals("void");
		StringBuilder buffer = new StringBuilder();
		StringBuilder methodParameterBuffer = new StringBuilder();

		buffer.append("CREATE OR REPLACE ")
				.append(isProcedure ? "PROCEDURE " : "FUNCTION ")
				.append(wrapperName)
				.append("(");

		for (DBJavaParameter argument : arguments) {
			appendVariableName(buffer, argument.getParameterType());
			appendSQLType(buffer,argument.getParameterType());

			methodParameterBuffer.append(argument.getParameterType());

			boolean isLast = arguments.indexOf(argument) == arguments.size() - 1;
			if (!isLast) {
				buffer.append(", ");
				methodParameterBuffer.append(", ");
			}
		}
		buffer.append(")");

		// Remove all empty parentheses "()"
		int index = buffer.indexOf("()");
		if (index != -1) {
			buffer.delete(index, index + 2);
		}

		String returnClass = javaMethod.getReturnType();

		if(returnClass.equals("class")){
			returnClass = javaMethod.getReturnClass().getName().replace("/",".");
		}

		if(!isProcedure) {
			buffer.append(" RETURN ");
			appendSQLType(buffer, returnClass);
		}

		buffer.append("\n");
		buffer.append("AS LANGUAGE JAVA NAME ");

		String schemaName = javaMethod.getSchemaName();
		String methodNameWithoutSchema = javaMethod.getQualifiedName().replace(schemaName + ".","");
		String methodNameWithoutOverload = methodNameWithoutSchema.split("#")[0];
		buffer.append("'")
				.append(methodNameWithoutOverload)
				.append("(")
				.append(methodParameterBuffer)
				.append(")")
				.append(isProcedure ? "" : " return " + returnClass)
				.append("'");

		buffer.append(";");

		DBNPreparedStatement<?> statement = conn.prepareCall(buffer.toString());
		context.setStatement(statement);
	}

	private void initDropWrapperCommand(JavaExecutionContext context) throws SQLException{
		DBNConnection conn = context.getConnection();

		DBJavaMethod javaMethod = getMethod();
		String wrapperName = javaMethod.getName().split("#")[0] + "_wrapper";

		String returnArgument = getReturnArgument();

		boolean isProcedure = returnArgument.equals("void");

		String buffer = "DROP " +
				(isProcedure ? "PROCEDURE " : "FUNCTION ") +
				wrapperName;

		DBNPreparedStatement<?> statement = conn.prepareCall(buffer);
		context.setStatement(statement);
	}

	private void initCommand(JavaExecutionContext context) throws SQLException {
		JavaExecutionInput executionInput = context.getInput();
		String command = buildExecutionCommand(executionInput);
		DBNConnection conn = context.getConnection();
		DBNPreparedStatement<?> statement = isQuery() ?
				conn.prepareStatement(command) :
				conn.prepareCall(command);

		context.setStatement(statement);
	}

	private void initLogging(JavaExecutionContext context) {
		JavaExecutionInput executionInput = context.getInput();
		DBDebuggerType debuggerType = context.getDebuggerType();
		ExecutionOptions options = executionInput.getOptions();

		ConnectionHandler connection = context.getTargetConnection();
		DBNConnection conn = context.getConnection();

		DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(getProject());
		boolean logging =
				debuggerType != DBDebuggerType.JDBC &&
						options.is(ExecutionOption.ENABLE_LOGGING) &&
						loggingManager.supportsLogging(connection) &&
						loggingManager.enableLogger(connection, conn);

		context.setLogging(logging);
	}

	private void initParameters(JavaExecutionContext context) throws SQLException {
		JavaExecutionInput executionInput = context.getInput();
		DBNPreparedStatement<?> statement = context.getStatement();
		bindParameters(executionInput, statement);
	}

	private void initTimeout(JavaExecutionContext context) throws SQLException {
		JavaExecutionInput executionInput = context.getInput();
		DBDebuggerType debuggerType = context.getDebuggerType();
		int timeout = debuggerType.isDebug() ?
				executionInput.getDebugExecutionTimeout() :
				executionInput.getExecutionTimeout();

		context.setTimeout(timeout);
		context.getStatement().setQueryTimeout(timeout);

	}

	private void execute(JavaExecutionContext context) throws SQLException {
		ConnectionHandler connection = nd(context.getTargetConnection());
		DBNConnection conn = context.getConnection();

		new CancellableDatabaseCall<JavaExecutionResult>(
				connection,
				conn,
				context.getTimeout(),
				TimeUnit.SECONDS) {

			@Override
			public JavaExecutionResult execute() throws SQLException {
				return executeStatement(context, getConnection());
			}

			@Override
			public void cancel() {
				Resources.cancel(context.getStatement());
			}
		}.start();

		JavaExecutionInput executionInput = context.getInput();
		SessionId targetSessionId = executionInput.getTargetSessionId();
		if (targetSessionId != SessionId.POOL) conn.notifyDataChanges(getMethod().getVirtualFile());

	}

	@Nullable
	private JavaExecutionResult executeStatement(JavaExecutionContext context, ConnectionHandler connection) throws SQLException {
		DBNPreparedStatement<?> statement = context.getStatement();
		statement.execute();

		JavaExecutionInput executionInput = context.getInput();
		JavaExecutionResult executionResult = executionInput.getExecutionResult();
		if (executionResult != null) {
			loadValues(executionResult, statement);
			executionResult.calculateExecDuration();

			if (context.isLogging()) {
				DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(context.getProject());
				String logOutput = loggingManager.readLoggerOutput(connection, context.getConnection());
				executionResult.setLogOutput(logOutput);
			}
		}
		return executionResult;
	}

	@NotNull
	private ConnectionHandler getConnection() {
		return getMethod().getConnection();
	}

	protected boolean isQuery() {
		return getArgumentsCount() > 0;
	}

	protected void bindParameters(JavaExecutionInput executionInput, DBNPreparedStatement<?> preparedStatement) throws SQLException {

	}

	public void loadValues(JavaExecutionResult executionResult, DBNPreparedStatement<?> preparedStatement) throws SQLException {
		for (DBJavaParameter argument : getArguments()) {
			if (/*argument.isOutput() &&*/ preparedStatement instanceof CallableStatement) {
				CallableStatement callableStatement = (CallableStatement) preparedStatement;
				Object result = callableStatement.getObject(argument.getPosition());
				executionResult.addArgumentValue(argument, result);
			}
		}
	}

	private Project getProject() {
		DBJavaMethod method = getMethod();
		return method.getProject();
	}

	public abstract String buildExecutionCommand(JavaExecutionInput executionInput) throws SQLException;
}
