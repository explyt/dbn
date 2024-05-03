package com.dbn.common.event;

import java.util.EventListener;

public interface ToggleListener extends EventListener {
    void toggled(boolean value);
}
