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

package com.dbn.oracleAI.intro.ui;

import com.dbn.common.Availability;
import com.dbn.common.AvailabilityInfo;
import com.dbn.common.event.ProjectEvents;
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
import com.dbn.oracleAI.ui.ChatBoxForm;
import com.dbn.oracleAI.ui.ChatBoxState;
import com.intellij.util.ui.AsyncProcessIcon;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

import static com.dbn.common.Priority.HIGHEST;
import static com.dbn.common.util.Conditional.when;

/**
 * Database Assistant initialization form
 * This form is presented to the user on top of the chat-box before the availability of the AI-Assistant is evaluated.
 * Evaluation of availability may fail due to connectivity and other reasons. This form allows recovery from those situations.
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class InitializationForm extends DBNFormBase {
    private JPanel initializingIconPanel;
    private JPanel mainPanel;
    private JPanel initializingPanel;
    private JPanel unsupportedPanel;
    private JPanel reinitializePanel;
    private JButton retryButton;
    private JPanel messagePanel;

    public InitializationForm(@Nullable IntroductionForm parent) {
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
        return getCurrentAvailability() == Availability.AVAILABLE;
    }

    private Availability getCurrentAvailability() {
        ChatBoxForm chatBox = getChatBox();
        ChatBoxState chatBoxState = chatBox.getState();
        return chatBoxState.getAvailability();
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
     * @param availabilityInfo the {@link AvailabilityInfo} resulted from the evaluation
     */
    private void updateComponents(AvailabilityInfo availabilityInfo) {
        initializingPanel.setVisible(false);
        unsupportedPanel.setVisible(false);
        reinitializePanel.setVisible(false);
        messagePanel.removeAll();

        Availability availability = availabilityInfo.getAvailability();
        if (availability == Availability.UNAVAILABLE) {
            unsupportedPanel.setVisible(true);
        } else if (availability == Availability.UNCERTAIN) {
            reinitializePanel.setVisible(true);
            String messageContent = "Could not initialize Database Assistant\n\n" + availabilityInfo.getMessage();
            TextContent message = TextContent.plain(messageContent);
            DBNHintForm messageForm = new DBNHintForm(this, message, MessageType.ERROR, true);
            messagePanel.add(messageForm.getComponent());
        }
        getIntroductionForm().evaluateAvailability();
    }


    /**
     * Verifies the availability of the AI Assistant if not already known and captured in the {@link ChatBoxState}
     * @return an {@link AvailabilityInfo} object
     */
    private AvailabilityInfo doCheckAvailability() {
        Availability availability = getCurrentAvailability();
        String availabilityMessage = null;

        ChatBoxForm chatBox = getChatBox();
        ChatBoxState chatBoxState = chatBox.getState();

        if (availability == Availability.UNCERTAIN) {
            ConnectionHandler connection = chatBox.getConnection();
            if (!DatabaseFeature.AI_ASSISTANT.isSupported(connection)) {
                // known already to bot be supported by the given database type
                availability = Availability.UNAVAILABLE;
            } else {
                // perform deep verification by accessing the database
                try {
                    boolean available =  checkAvailability(connection);
                    availability = available ? Availability.AVAILABLE : Availability.UNAVAILABLE;
                } catch (Throwable e) {
                    // availability remains uncertain at this stage as it could bot be verified against the database
                    Diagnostics.conditionallyLog(e);
                    availabilityMessage = e.getMessage();
                }
            }
        }


        chatBoxState.setAvailability(availability);
        return new AvailabilityInfo(availability, availabilityMessage);
    }

    private static boolean checkAvailability(ConnectionHandler connection) throws SQLException {
        return DatabaseInterfaceInvoker.load(HIGHEST,
                "Loading metadata",
                "Verifying database companion feature support",
                connection.getProject(),
                connection.getConnectionId(),
                conn -> connection.getAssistantInterface().isAssistantFeatureSupported(conn));
    }

    private ChatBoxForm getChatBox() {
        return getIntroductionForm().getChatBox();
    }

    private IntroductionForm getIntroductionForm() {
        return ensureParentComponent();
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

}
