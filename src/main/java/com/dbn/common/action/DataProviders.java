package com.dbn.common.action;

import com.dbn.common.compatibility.Workaround;
import com.dbn.common.dispose.Checks;
import com.dbn.common.ref.WeakRef;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

@Workaround
public class DataProviders {

    public static void register(@NotNull JComponent component, @NotNull DataProviderDelegate delegate) {
        if (component instanceof DataProvider) return;

        DataProvider dataProvider = createDataProvider(delegate);
        DataManager.registerDataProvider(component, dataProvider);
    }

    private static DataProvider createDataProvider(DataProviderDelegate delegate) {
        return new DataProvider() {
            private final WeakRef<DataProviderDelegate> weakRefDelegate = WeakRef.of(delegate);

            @Override
            public @Nullable Object getData(@NotNull String dataId) {
                DataProviderDelegate dataProvider = weakRefDelegate.get();
                if (dataProvider == null) return null;
                if (Checks.isNotValid(dataProvider)) {
                    weakRefDelegate.clear();
                    return null;
                }
                return dataProvider.getData(dataId);
            }
        };
    }
}
