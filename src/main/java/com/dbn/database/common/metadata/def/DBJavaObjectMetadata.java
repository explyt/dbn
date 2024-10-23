package com.dbn.database.common.metadata.def;

import com.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBJavaObjectMetadata extends DBObjectMetadata {
	String getName() throws SQLException;

	boolean isFinal() throws SQLException;;

	boolean isAbstract()throws SQLException;;

	boolean isStatic()throws SQLException;;

	boolean isInner()throws SQLException;;

	String getKind()throws SQLException;;
// {CLASS, ENUM, INTERFACE} - "CLASS" , "ENUM", "INTERFACE"

	String getAccessibility()throws SQLException;;
// { PUBLIC, PRIVATE, PROTECTED, NULL}
}
