package com.dbn.common.util;


import com.dbn.common.routine.ParametricRunnable;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isNotValid;
import static com.dbn.common.dispose.Failsafe.guarded;

@UtilityClass
public class Conditional {

    public static void when(boolean condition, @Nullable Runnable runnable) {
        if (runnable == null) return;
        if (!condition) return;

        guarded(runnable, r -> r.run());
    }

    public static <T, E extends Throwable> void whenValid(@Nullable T object, ParametricRunnable<T, E> runnable) throws E{
        if (runnable == null) return;
        if (isNotValid(object)) return;

        guarded(object, runnable);
    }

    public static <T, E extends Throwable> void whenNotNull(@Nullable T object, ParametricRunnable<T, E> runnable) throws E{
        if (runnable == null) return;
        if (object == null) return;

        guarded(object, runnable);;
    }

    public static <E extends Throwable> void whenNotEmpty(@Nullable String string, ParametricRunnable<String, E> runnable) throws E{
        if (runnable == null) return;
        if (Strings.isEmptyOrSpaces(string)) return;

        guarded(string, runnable);
    }
}
