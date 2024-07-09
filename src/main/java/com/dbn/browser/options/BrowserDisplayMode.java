package com.dbn.browser.options;

import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;


@Getter
@AllArgsConstructor
public enum BrowserDisplayMode implements Presentable{
    SIMPLE(txt("app.browser.const.DisplayMode_SIMPLE")),
    TABBED(txt("app.browser.const.DisplayMode_TABBED")),
    SELECTOR(txt("app.browser.const.DisplayMode_SELECTOR"));

    private final String name;
}
