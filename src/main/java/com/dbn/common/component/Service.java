package com.dbn.common.component;

import com.dbn.common.dispose.StatefulDisposable;
import com.dbn.nls.NlsSupport;
import com.intellij.openapi.components.NamedComponent;
import com.intellij.openapi.project.DumbAware;

public interface Service extends DumbAware, NamedComponent, StatefulDisposable, NlsSupport {

    @Override
    default void disposeInner() {
        nullify();
    }

}
