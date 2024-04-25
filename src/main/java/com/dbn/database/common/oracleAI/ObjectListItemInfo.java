package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.ObjectListItem;
import com.dbn.oracleAI.config.ObjectListItemManager;
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

  private final String OWNER = "OWNER";
  private final String TABLE_NAME = "TABLE_NAME";

  public ObjectListItemInfo(String profileName) {
    this.profileName = profileName;
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
      String table = rs.getString(TABLE_NAME);
      objectListItemsList.add(ObjectListItemManager.getObjectListItem(owner, table));
    }
  }
}
