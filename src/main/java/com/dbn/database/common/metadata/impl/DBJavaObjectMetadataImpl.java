package com.dbn.database.common.metadata.impl;

import com.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dbn.database.common.metadata.def.DBJavaObjectMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBJavaObjectMetadataImpl extends DBObjectMetadataBase implements DBJavaObjectMetadata {

	public DBJavaObjectMetadataImpl(ResultSet resultSet) {
		super(resultSet);
	}

	@Override
	public String getName() throws SQLException {
		return getString("NAME");
	}

	@Override
	public boolean isFinal() throws SQLException {
		return isYesFlag("IS_FINAL");
	}

	@Override
	public boolean isAbstract() throws SQLException {
		return isYesFlag("IS_ABSTRACT");
	}

	@Override
	public boolean isStatic() throws SQLException {
		return isYesFlag("IS_STATIC");
	}

	@Override
	public boolean isInner() throws SQLException {
		return isYesFlag("IS_INNER");
	}

	@Override
	public String getKind() throws SQLException {
		return getString("KIND");
	}

	@Override
	public String getAccessibility() throws SQLException {
		return getString("ACCESSIBILITY");
	}
}
