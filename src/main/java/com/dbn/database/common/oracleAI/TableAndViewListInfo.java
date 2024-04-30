package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.types.DatabaseObjectType;
import lombok.Getter;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TableAndViewListInfo implements CallableStatementOutput {
  private List<DBObjectItem> DBObjectListItems;
  private String schemaName;
  private final DatabaseObjectType type;

  private final String OBJ_OWNER_COLUMN_NANE = "OWNER";


  public TableAndViewListInfo(String schemaName, DatabaseObjectType type) {
    this.schemaName = schemaName;
    this.type = type;
  }

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    DBObjectListItems = new ArrayList<>();

    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      //String owner = rs.getString(OBJ_OWNER_COLUMN_NANE);
      String tableName = rs.getString(type.getColumnName());
      DBObjectListItems.add(new DBObjectItem(this.schemaName, tableName, type));
    }
  }
}
