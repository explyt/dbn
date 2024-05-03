package com.dbn.common.util;

import com.dbn.common.thread.Dispatch;
import com.dbn.common.ui.dialog.DBNDialog;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.dbn.common.dispose.Checks.isNotValid;
import static com.dbn.common.ui.progress.ProgressDialogHandler.closeProgressDialogs;

@UtilityClass
public class Dialogs {

    public static <T extends DBNDialog<?>> void show(@NotNull Supplier<T> builder) {
        show(builder, null);
    }

    public static <T extends DBNDialog<?>> void show(@NotNull Supplier<T> builder, @Nullable DialogCallback<T> callback) {
        Dispatch.run(true, () -> {
            closeProgressDialogs();
            T dialog = builder.get();
            dialog.setDialogCallback(callback);
            dialog.show();
        });
    }

    public static <T extends DBNDialog> void close(@Nullable T dialog, int exitCode) {
        if (isNotValid(dialog)) return;
        Dispatch.run(true, () -> dialog.close(exitCode));
    }

    @FunctionalInterface
    public interface DialogCallback<T extends DBNDialog<?>> {
        void call(T dialog, int exitCode);
    }

}
