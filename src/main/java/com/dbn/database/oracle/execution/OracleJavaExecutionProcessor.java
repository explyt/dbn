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

package com.dbn.database.oracle.execution;

import com.dbn.connection.jdbc.DBNPreparedStatement;
import com.dbn.database.common.execution.JavaExecutionProcessorImpl;
import com.dbn.execution.java.JavaExecutionInput;
import com.dbn.execution.java.result.JavaExecutionResult;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBJavaParameter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class OracleJavaExecutionProcessor extends JavaExecutionProcessorImpl {


	public OracleJavaExecutionProcessor(DBJavaMethod method) {
		super(method);
	}

	protected void preHookExecutionCommand(StringBuilder buffer) {}
	protected void postHookExecutionCommand(StringBuilder buffer) {}

	public static final HashMap<String, String> dataTypeMap = new HashMap<>();

	// see link
	// https://docs.oracle.com/en/database/oracle/oracle-database/21/jjdbc/accessing-and-manipulating-Oracle-data.html#GUID-1AF80C90-DFE6-4A3E-A407-52E805726778
	static {
		dataTypeMap.put("String", "VARCHAR2");
		dataTypeMap.put("java.lang.String", "VARCHAR2");
		dataTypeMap.put("boolean", "NUMBER");
		dataTypeMap.put("byte", "NUMBER");
		dataTypeMap.put("char", "VARCHAR2");
		dataTypeMap.put("short", "NUMBER");
		dataTypeMap.put("int", "NUMBER");
		dataTypeMap.put("long", "NUMBER");
		dataTypeMap.put("float", "NUMBER");
		dataTypeMap.put("double", "BINARY_DOUBLE");
		dataTypeMap.put("byte[]", "RAW");
		
		dataTypeMap.put("java.sql.Date", "DATE");
		dataTypeMap.put("java.sql.Time", "DATE");
		dataTypeMap.put("java.math.BigDecimal", "NUMBER");
		dataTypeMap.put("java.math.BigInteger", "NUMBER");

		//some Java Wrapper Types
		dataTypeMap.put("java.lang.Boolean", "NUMBER");
		dataTypeMap.put("java.lang.Byte", "NUMBER");
		dataTypeMap.put("java.lang.Character", "CHAR");
		dataTypeMap.put("java.lang.Short", "NUMBER");
		dataTypeMap.put("java.lang.Integer", "NUMBER");
		dataTypeMap.put("java.lang.Long", "NUMBER");
		dataTypeMap.put("java.lang.Float", "NUMBER");
		dataTypeMap.put("java.lang.Double", "BINARY_DOUBLE");

	}

	@Override
	public String buildExecutionCommand(JavaExecutionInput executionInput) throws SQLException {
		DBJavaMethod javaMethod = getMethod();
		String wrapperName = javaMethod.getName().split("#")[0] + "_wrapper";

		String returnArgument = getReturnArgument();
		List<DBJavaParameter> arguments = getArguments();

		boolean isProcedure = returnArgument.equals("void");
		StringBuilder buffer = new StringBuilder();

		StringBuilder methodCallPrepare = new StringBuilder();

		for (DBJavaParameter argument : arguments) {
				methodCallPrepare.append("?");

				boolean isLast = arguments.indexOf(argument) == arguments.size() - 1;
				if (!isLast) {
					methodCallPrepare.append(", ");
				}
		}

		buffer.append("declare\n");
		buffer.append("begin \n");

		preHookExecutionCommand(buffer);

		if(isProcedure){
			buffer.append(wrapperName)
					.append("(")
					.append(methodCallPrepare)
					.append(");\n");

			// Remove all empty parentheses "()"
			int index = buffer.lastIndexOf("()");
			if (index != -1) {
				buffer.delete(index, index + 2);
			}
		} else {
			buffer.append("    dbms_output.put_line(")
					.append(wrapperName)
					.append("(")
					.append(methodCallPrepare)
					.append(")")
					.append(");\n");
		}
		postHookExecutionCommand(buffer);
		buffer.append("end;\n");

		return buffer.toString();
	}

	@Override
	protected void bindParameters(JavaExecutionInput executionInput, DBNPreparedStatement<?> callableStatement) throws SQLException {

		// bind input variables
		int parameterIndex = 1;
		for (DBJavaParameter argument : getArguments()) {

			String clazz = argument.getParameterType();
			String value = executionInput.getInputValue(argument, "");
			if(clazz.equals("String")) callableStatement.setString(parameterIndex, value); else
			if(clazz.equals("byte")) callableStatement.setByte(parameterIndex, Byte.parseByte(value)); else
			if(clazz.equals("short")) callableStatement.setShort(parameterIndex, Short.parseShort(value)); else
			if(clazz.equals("int")) callableStatement.setInt(parameterIndex, Integer.parseInt(value)); else
			if(clazz.equals("long")) callableStatement.setLong(parameterIndex, Long.parseLong(value)); else
			if(clazz.equals("float")) callableStatement.setFloat(parameterIndex, Float.parseFloat(value)); else
			if(clazz.equals("double")) callableStatement.setDouble(parameterIndex, Double.parseDouble(value)); else
			if(clazz.equals("boolean")) callableStatement.setBoolean(parameterIndex, Boolean.getBoolean(value)); else
				callableStatement.setObject(parameterIndex, value);


//			DBType type = dataType.getDeclaredType();
//			if (dataType.isPurelyDeclared()) {
//				List<DBTypeAttribute> attributes = type.getAttributes();
//				for (DBTypeAttribute attribute : attributes) {
//					String stringValue = executionInput.getInputValue(argument, "");
//					String dataType = clazz;
//					System.out.println("--");
//					System.out.println(stringValue);
//					setParameterValue(callableStatement, parameterIndex, dataType, value);
//					parameterIndex++;
//				}
//			}
			parameterIndex++;
		}
	}

	@Override
	public void loadValues(JavaExecutionResult executionResult, DBNPreparedStatement<?> preparedStatement) throws SQLException {

	}
}
