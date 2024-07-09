package com.dbn.connection;

import com.dbn.common.constant.Constant;
import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum DatabaseUrlType implements Presentable, Constant<DatabaseUrlType> {
    TNS(txt("cfg.connection.const.DatabaseUrlType_TNS")),
    SID(txt("cfg.connection.const.DatabaseUrlType_SID")),
    SERVICE(txt("cfg.connection.const.DatabaseUrlType_SERVICE")),
    LDAP(txt("cfg.connection.const.DatabaseUrlType_LDAP")),
    LDAPS(txt("cfg.connection.const.DatabaseUrlType_LDAPS")),
    DATABASE(txt("cfg.connection.const.DatabaseUrlType_DATABASE")),
    CUSTOM(txt("cfg.connection.const.DatabaseUrlType_CUSTOM")),
    FILE(txt("cfg.connection.const.DatabaseUrlType_FILE"));

    private final String name;
}
