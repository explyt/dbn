package com.dbn.nls;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.PropertyKey;

public interface NlsSupport {
    default @Nls String txt(@Nls @PropertyKey(resourceBundle = NlsResources.BUNDLE) String key) {
        return NlsResources.txt(key);
    }

    default @Nls String txt(@Nls @PropertyKey(resourceBundle = NlsResources.BUNDLE) String key, Object... params) {
        return NlsResources.txt(key, params);
    }
}
