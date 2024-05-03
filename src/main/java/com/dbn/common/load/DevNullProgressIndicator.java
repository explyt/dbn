package com.dbn.common.load;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class DevNullProgressIndicator implements ProgressIndicator {
    public static final DevNullProgressIndicator INSTANCE = new DevNullProgressIndicator();

    private DevNullProgressIndicator() {}

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public void setText2(String text) {

    }

    @Override
    public String getText2() {
        return null;
    }

    @Override
    public double getFraction() {
        return 0;
    }

    @Override
    public void setFraction(double fraction) {

    }

    @Override
    public void pushState() {

    }

    @Override
    public void popState() {

    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public @NotNull ModalityState getModalityState() {
        return ModalityState.NON_MODAL;
    }

    @Override
    public void setModalityProgress(@Nullable ProgressIndicator modalityProgress) {

    }

    @Override
    public boolean isIndeterminate() {
        return false;
    }

    @Override
    public void setIndeterminate(boolean indeterminate) {

    }

    @Override
    public void checkCanceled() {

    }

    @Override
    public boolean isPopupWasShown() {
        return false;
    }

    @Override
    public boolean isShowing() {
        return false;
    }

}
