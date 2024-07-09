package com.dbn.data.grid.options;

import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum NullSortingOption implements Presentable{
    FIRST(txt("cfg.data.const.NullSortingOption_FIRST")),
    LAST(txt("cfg.data.const.NullSortingOption_LAST"));

    private final String name;
}
