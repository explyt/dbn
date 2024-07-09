package com.dbn.connection.transaction;

import com.dbn.common.icon.Icons;
import com.dbn.common.option.InteractiveOption;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.*;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum TransactionOption implements InteractiveOption{
    ASK(txt("cfg.connection.const.TransactionOption_ASK"), null),
    COMMIT(txt("cfg.connection.const.TransactionOption_COMMIT"), Icons.CONNECTION_COMMIT),
    ROLLBACK(txt("cfg.connection.const.TransactionOption_ROLLBACK"), Icons.CONNECTION_ROLLBACK),
    REVIEW_CHANGES(txt("cfg.connection.const.TransactionOption_REVIEW_CHANGES"), null),
    CANCEL(txt("cfg.connection.const.TransactionOption_CANCEL"), null);

    private final String name;
    private final Icon icon;

    @Override
    public boolean isCancel() {
        return this == CANCEL;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }
}
