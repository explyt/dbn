package com.dbn.connection;

import com.dbn.common.constant.Constant;
import com.dbn.common.ui.Presentable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum AuthenticationType implements Constant<AuthenticationType>, Presentable {
    NONE("None"),
    USER("User"),
    USER_PASSWORD("User / Password"),
    OS_CREDENTIALS("OS Credentials"),
    TOKEN_AUTHENTICATION("Token Authentication");

    private final String name;
}
