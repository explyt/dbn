package com.dbn.connection.ssh;

import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum SshAuthType implements Presentable{
    PASSWORD(txt("cfg.connection.const.SshAuthType_PASSWORD")),
    KEY_PAIR(txt("cfg.connection.const.SshAuthType_KEY_PAIR"));

    private final String name;
}
