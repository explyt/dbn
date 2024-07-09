package com.dbn.common.action;

import com.dbn.common.compatibility.Workaround;

@Workaround // DataProvider override-only assertions
public interface DataProviderDelegate {
    Object getData(String dataId);
}
