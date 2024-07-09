package com.dbn.data.record.navigation;

import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.*;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum RecordNavigationTarget implements Presentable{
    VIEWER(txt("cfg.data.const.RecordNavigationTarget_VIEWER"), null),
    EDITOR(txt("cfg.data.const.RecordNavigationTarget_EDITOR"), null),
    ASK(txt("cfg.data.const.RecordNavigationTarget_ASK"), null),
    PROMPT(txt("cfg.data.const.RecordNavigationTarget_PROMPT"), null);

    private final String name;
    private final Icon icon;
}
