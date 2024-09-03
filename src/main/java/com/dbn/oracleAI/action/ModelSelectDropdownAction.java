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
import com.dbn.common.util.Lists;
import com.dbn.oracleAI.AIProfileItem;
import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.ui.ChatBoxForm;
import com.dbn.oracleAI.ui.ChatBoxState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * Action for selecting the current AI-assistant model
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class ModelSelectDropdownAction extends DBNComboBoxAction implements DumbAware {
    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component, DataContext dataContext) {
        List<ProviderModel> models = getProviderModels(dataContext);

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        Lists.forEach(models, m -> actionGroup.add(new ModelSelectAction(m)));

        return actionGroup;
    }

    private List<ProviderModel> getProviderModels(DataContext dataContext) {
        ChatBoxForm chatBox = dataContext.getData(DataKeys.ASSISTANT_CHAT_BOX);
        if (chatBox == null) return Collections.emptyList();

        ChatBoxState state = chatBox.getState();

        AIProfileItem profile = state.getSelectedProfile();
        if (profile == null) return Collections.emptyList();

        return profile.getProvider().getModels();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ChatBoxForm chatBox = e.getData(DataKeys.ASSISTANT_CHAT_BOX);
        boolean enabled = chatBox != null && chatBox.getState().promptingEnabled();

        Presentation presentation = e.getPresentation();
        presentation.setText(getText(e));
        presentation.setDescription(txt("companion.chat.model.tooltip"));
        presentation.setEnabled(enabled);
    }

    private String getText(@NotNull AnActionEvent e) {
        ChatBoxForm chatBox = e.getData(DataKeys.ASSISTANT_CHAT_BOX);
        if (chatBox == null) return "Model";

        String text = getSelectedModelName(e);
        if (text != null) return text;

        List<ProviderModel> models = getProviderModels(e.getDataContext());
        if (!models.isEmpty()) return "Select Model";

        return "Model";
    }

    private static String getSelectedModelName(@NotNull AnActionEvent e) {
        ChatBoxForm chatBox = e.getData(DataKeys.ASSISTANT_CHAT_BOX);
        if (chatBox == null) return null;

        ChatBoxState state = chatBox.getState();
        AIProfileItem profile = state.getSelectedProfile();
        if (profile == null) return null;

        ProviderModel model = profile.getModel();
        if (model == null) return null;

        return Actions.adjustActionName(model.name());
    }

    @Override
    protected boolean shouldShowDisabledActions() {
        return true;
    }
}
