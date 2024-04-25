package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.ObjectListItem;
import com.dbn.oracleAI.config.ObjectListItemManager;
import com.dbn.oracleAI.types.DataType;
import lombok.Getter;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ObjectListItemInfo implements CallableStatementOutput {
  private List<ObjectListItem> objectListItemsList;
  private String profileName;
  private final DataType type;

  private final String OWNER = "OWNER";

  public ObjectListItemInfo(String profileName, DataType type) {
    this.profileName = profileName;
    this.type = type;
  }

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    objectListItemsList = new ArrayList<>();

    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      String owner = rs.getString(OWNER);
      String table = rs.getString(type.getAction());
      objectListItemsList.add(ObjectListItemManager.getObjectListItem(owner, table, type));
    }
  }
}
