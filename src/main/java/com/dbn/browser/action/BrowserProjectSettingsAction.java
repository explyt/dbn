package com.dbn.browser.action;

import com.dbn.options.ConfigId;
import com.dbn.options.action.ProjectSettingsOpenAction;

public class BrowserProjectSettingsAction extends ProjectSettingsOpenAction {
    public BrowserProjectSettingsAction() {
        super(ConfigId.CONNECTIONS, false);
    }
}
