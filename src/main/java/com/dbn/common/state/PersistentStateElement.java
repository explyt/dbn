package com.dbn.common.state;


import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import static com.dbn.common.options.setting.Settings.newElement;

public interface PersistentStateElement {
    void readState(@NonNls Element element);
    void writeState(@NonNls Element element);

    static <T extends PersistentStateElement> T cloneElement(T source, T target) {
        Element element = newElement("Element");
        source.writeState(element);
        target.readState(element);
        return target;
    }
}
