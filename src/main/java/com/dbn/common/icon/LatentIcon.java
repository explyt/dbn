package com.dbn.common.icon;

import com.dbn.common.latent.Latent;
import com.dbn.common.util.Safe;
import com.intellij.openapi.util.ScalableIcon;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@Getter
abstract class LatentIcon implements ScalableIcon {
    private final String path;
    private final Latent<Icon> delegate = Latent.basic(() -> load());

    public LatentIcon(String path) {
        this.path = path;
    }

    @Override
    public float getScale() {
        return Safe.call(delegate(), ScalableIcon.class, i -> i.getScale(), 1F);
    }

    @Override
    public @NotNull Icon scale(float scaleFactor) {
        return Safe.call(delegate(), ScalableIcon.class, i -> i.scale(scaleFactor), delegate());
    }

    protected abstract Icon load();

    @Delegate
    private ScalableIcon delegate() {
        return (ScalableIcon) delegate.get();
    }
}
