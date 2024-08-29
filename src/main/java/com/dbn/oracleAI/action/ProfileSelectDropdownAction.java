/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI.action;

import com.dbn.common.action.DataKeys;
import com.dbn.common.ui.misc.DBNComboBoxAction;
import com.dbn.common.util.Actions;
import com.dbn.oracleAI.AIProfileItem;
import com.dbn.oracleAI.ui.OracleAIChatBox;
import com.dbn.oracleAI.ui.OracleAIChatBoxState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * Action for selecting the current AI-assistant profile
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class ProfileSelectDropdownAction extends DBNComboBoxAction implements DumbAware {

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component, DataContext dataContext) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        OracleAIChatBox chatBox = dataContext.getData(DataKeys.COMPANION_CHAT_BOX);
        if (chatBox == null) return actionGroup;

        OracleAIChatBoxState state = chatBox.getState();
        List<AIProfileItem> profiles = state.getProfiles();
        profiles.forEach(p -> actionGroup.add(new ProfileSelectAction(p)));
        actionGroup.addSeparator();

        actionGroup.add(new ProfilesSetupAction());
        return actionGroup;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        OracleAIChatBox chatBox = e.getData(DataKeys.COMPANION_CHAT_BOX);
        boolean enabled = chatBox != null && chatBox.getState().promptingEnabled();

        Presentation presentation = e.getPresentation();
        presentation.setText(getText(e));
        presentation.setDescription(txt("companion.chat.profile.tooltip"));
        presentation.setEnabled(enabled);
    }

    private String getText(@NotNull AnActionEvent e) {
        OracleAIChatBox chatBox = e.getData(DataKeys.COMPANION_CHAT_BOX);
        if (chatBox == null) return "Profile";

        String text = getSelectedProfileName(e);
        if (text != null) return text;

        List<AIProfileItem> profiles = chatBox.getState().getProfiles();
        if (!profiles.isEmpty()) return "Select Profile";

        return "Profile";
    }

    private static String getSelectedProfileName(@NotNull AnActionEvent e) {
        OracleAIChatBox chatBox = e.getData(DataKeys.COMPANION_CHAT_BOX);
        if (chatBox == null) return null;

        OracleAIChatBoxState state = chatBox.getState();
        AIProfileItem profile = state.getSelectedProfile();
        if (profile == null) return null;

        return Actions.adjustActionName(profile.getName());
    }

    @Override
    protected boolean shouldShowDisabledActions() {
        return true;
    }
}
