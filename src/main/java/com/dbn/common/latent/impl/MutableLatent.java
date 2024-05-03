package com.dbn.common.latent.impl;


import com.dbn.common.latent.Latent;
import com.dbn.common.latent.Loader;
import com.dbn.common.util.Safe;

import java.util.Objects;

public class MutableLatent<T, M> extends BasicLatent<T> implements Latent<T> {
    private M mutable;
    private final Loader<M> mutableLoader;

    public MutableLatent(Loader<M> mutableLoader, Loader<T> loader) {
        super(loader);
        this.mutableLoader = mutableLoader;
    }

    @Override
    protected boolean shouldLoad(){
        if (super.shouldLoad()) return true;

        return mutable != null && !Objects.equals(mutable, loadMutable());
    }

    @Override
    protected void beforeLoad() {
        mutable = loadMutable();
    }

    private M loadMutable() {
        return Safe.call(mutableLoader, ml -> ml.load());
    }
}
