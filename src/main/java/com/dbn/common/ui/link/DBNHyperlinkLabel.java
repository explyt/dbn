/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.common.ui.link;

import com.dbn.common.ui.listener.ToggleBorderOnFocusListener;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ui.PlatformColors;
import lombok.Setter;

import static com.intellij.util.ui.JBUI.Borders.customLine;
import static com.intellij.util.ui.JBUI.Borders.empty;

@Setter
public class DBNHyperlinkLabel extends HyperlinkLabel {
    public DBNHyperlinkLabel() {
        setFocusable(true);
        setRequestFocusEnabled(true);
        addFocusListener(new ToggleBorderOnFocusListener(
                empty(),
                customLine(PlatformColors.BLUE, 0, 0, 1, 0)));
    }
}
