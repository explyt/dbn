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

package com.dbn.assistant.init.ui;

import com.dbn.assistant.DatabaseAssistantType;
import com.dbn.assistant.chat.window.ui.ChatBoxForm;
import com.dbn.assistant.state.AssistantState;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.feature.FeatureAvailability;
import com.dbn.common.feature.FeatureAvailabilityInfo;
import com.dbn.common.message.MessageType;
import com.dbn.common.text.TextContent;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.form.DBNHintForm;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.config.ConnectionConfigListener;
import com.dbn.database.DatabaseFeature;
import com.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dbn.diagnostics.Diagnostics;
import com.intellij.util.ui.AsyncProcessIcon;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

import static com.dbn.common.Priority.HIGHEST;
import static com.dbn.common.feature.FeatureAvailability.*;
import static com.dbn.common.util.Conditional.when;

/**
 * Database Assistant initialization form
 * This form is presented to the user on top of the chat-box before the availability of the AI-Assistant is evaluated.
 * Evaluation of availability may fail due to connectivity and other reasons. This form allows recovery from those situations.
 *
 * @author Dan Cioca (Oracle)
 */
public class AssistantInitializationForm extends DBNFormBase {
    private JPanel initializingIconPanel;
    private JPanel mainPanel;
    private JPanel initializingPanel;
    private JPanel unsupportedPanel;
    private JPanel reinitializePanel;
    private JButton retryButton;
    private JPanel messagePanel;

    public AssistantInitializationForm(@Nullable AssistantIntroductionForm parent) {
        super(parent);
        initializingIconPanel.add(new AsyncProcessIcon("Loading"), BorderLayout.CENTER);
        retryButton.addActionListener(e -> checkAvailability());

        checkAvailability();

        //
        ProjectEvents.subscribe(ensureProject(), this,
                ConnectionConfigListener.TOPIC,
                ConnectionConfigListener.whenSetupChanged(() -> when(!isAvailable(), () -> checkAvailability())));
    }

    private boolean isAvailable() {
        return getCurrentAvailability() == AVAILABLE;
    }

    private FeatureAvailability getCurrentAvailability() {
        ChatBoxForm chatBox = getChatBox();
        AssistantState assistantState = chatBox.getAssistantState();
        return assistantState.getAvailability();
    }

    /**
     * Invokes the availability evaluation in background and updates the UI components afterward
     */
    private void checkAvailability() {
        // reset component visibility
        unsupportedPanel.setVisible(false);
        reinitializePanel.setVisible(false);
        initializingPanel.setVisible(true);

        Dispatch.async(getProject(), mainPanel,
                () -> doCheckAvailability(),
                a -> updateComponents(a));
    }

    /**
     * Updates UI components after availability evaluation
     * @param availabilityInfo the {@link FeatureAvailabilityInfo} resulted from the evaluation
     */
    private void updateComponents(FeatureAvailabilityInfo availabilityInfo) {
        initializingPanel.setVisible(false);
        unsupportedPanel.setVisible(false);
        reinitializePanel.setVisible(false);
        messagePanel.removeAll();

        FeatureAvailability availability = availabilityInfo.getAvailability();
        if (availability == UNAVAILABLE) {
            unsupportedPanel.setVisible(true);
        } else if (availability == UNCERTAIN) {
            reinitializePanel.setVisible(true);
            String messageContent = "Could not initialize Database Assistant\n\n" + availabilityInfo.getMessage();
            TextContent message = TextContent.plain(messageContent);
            DBNHintForm messageForm = new DBNHintForm(this, message, MessageType.ERROR, true);
            messagePanel.add(messageForm.getComponent());
        }
        getIntroductionForm().evaluateAvailability();
    }


    /**
     * Verifies the availability of the AI Assistant if not already known and captured in the {@link AssistantState}
     * @return an {@link FeatureAvailabilityInfo} object
     */
    private FeatureAvailabilityInfo doCheckAvailability() {
        DatabaseAssistantType assistantType = getChatBox().getAssistantState().getAssistantType();
        FeatureAvailability availability = getCurrentAvailability();
        String availabilityMessage = null;

        ChatBoxForm chatBox = getChatBox();
        AssistantState assistantState = chatBox.getAssistantState();

        if (availability == UNCERTAIN) {
            ConnectionHandler connection = chatBox.getConnection();
            if (!DatabaseFeature.AI_ASSISTANT.isSupported(connection)) {
                // known already to bot be supported by the given database type
                availability = UNAVAILABLE;
            } else {
                // perform deep verification by accessing the database
                try {
                    boolean available = checkAvailability(connection);
                    assistantType = resolveAssistantType(connection);

                    availability = available ? AVAILABLE : UNAVAILABLE;
                } catch (Throwable e) {
                    // availability remains uncertain at this stage as it could bot be verified against the database
                    Diagnostics.conditionallyLog(e);
                    availabilityMessage = e.getMessage();
                }
            }
        }

        assistantState.setAvailability(availability);
        assistantState.setAssistantType(assistantType);
        return new FeatureAvailabilityInfo(availability, availabilityMessage);
    }

    private static boolean checkAvailability(ConnectionHandler connection) throws SQLException {
        return DatabaseInterfaceInvoker.load(HIGHEST,
                "Loading metadata",
                "Verifying database assistant feature support",
                connection.getProject(),
                connection.getConnectionId(),
                conn -> connection.getAssistantInterface().isAssistantFeatureSupported(conn));
    }

    private static DatabaseAssistantType resolveAssistantType(ConnectionHandler connection) throws SQLException {
        return DatabaseInterfaceInvoker.load(HIGHEST,
                "Loading metadata",
                "Resolving database assistant type",
                connection.getProject(),
                connection.getConnectionId(),
                conn -> connection.getAssistantInterface().getAssistantType(conn));
    }

    private ChatBoxForm getChatBox() {
        return getIntroductionForm().getChatBox();
    }

    private AssistantIntroductionForm getIntroductionForm() {
        return ensureParentComponent();
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

}
