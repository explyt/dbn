package com.dbn.common.locale;

import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum DBNumberFormat implements Presentable{
    GROUPED(txt("cfg.shared.const.NumberFormat_GROUPED")),
    UNGROUPED(txt("cfg.shared.const.NumberFormat_UNGROUPED")),
    CUSTOM(txt("cfg.shared.const.NumberFormat_CUSTOM"));

    private final String name;
}
