package com.dci.intellij.dbn.common.latent;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.Disposable;

public abstract class DisposableLatent<T extends Disposable, P extends Disposable> extends Latent<T> {
    private P parent;

    private DisposableLatent(P parent) {
        super();
        this.parent = Failsafe.ensure(parent);
    }

    public static <T extends Disposable, P extends Disposable> DisposableLatent<T, P> create(P parent, Loader<T> loader) {
        return new DisposableLatent<T, P>(parent) {
            @Override
            public Loader<T> getLoader() {
                return loader;
            }
        };
    }

    @Override
    protected boolean shouldLoad() {
        Failsafe.ensure(parent);
        return super.shouldLoad();
    }

    @Override
    public void loaded(T value) {
        super.loaded(value);
        DisposerUtil.register(parent, value);
    }
}
