package com.dbn.navigation.object;

import com.dbn.common.consumer.SetCollector;
import com.dbn.common.dispose.StatefulDisposableBase;
import com.dbn.common.latent.Latent;
import com.dbn.common.sign.Signed;
import com.dbn.object.common.DBObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class DBObjectLookupData extends StatefulDisposableBase implements Signed {
    private final SetCollector<DBObject> data = SetCollector.concurrent();
    private final Latent<String[]> names = Latent.mutable(() -> data.size(), () -> buildNames());

    @Getter
    @Setter
    private int signature = -1;

    private String[] buildNames() {
        checkDisposed();
        return data.elements().
                stream().
                sorted().
                map(object -> object.getName()).
                distinct().
                toArray(String[]::new);
    }

    public String[] names() {
        return names.get();
    }

    public Object[] elements(String name) {
        checkDisposed();
        return data.elements().
                stream().
                filter(object -> Objects.equals(object.getName(), name)).
                sorted().
                toArray();
    }

    public void accept(DBObject object) {
        checkDisposed();
        data.accept(object);
    }


    @Override
    public void disposeInner() {
        data.clear();
        names.reset();
    }
}
