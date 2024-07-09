package com.dbn.editor.session.options;

import com.dbn.common.option.InteractiveOptionBroker;
import com.dbn.common.options.BasicConfiguration;
import com.dbn.common.options.setting.Settings;
import com.dbn.connection.operation.options.OperationSettings;
import com.dbn.editor.session.options.ui.SessionBrowserSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class SessionBrowserSettings extends BasicConfiguration<OperationSettings, SessionBrowserSettingsForm> {
    public static final String REMEMBER_OPTION_HINT = ""; //"\n\n(you can remember your option and change it at any time in Settings > Operations > Session Manager)";

    private boolean reloadOnFilterChange = false;
    private final InteractiveOptionBroker<SessionInterruptionOption> disconnectSession =
            new InteractiveOptionBroker<>(
                    "disconnect-session",
                    "app.sessions.title.DisconnectSessions",
                    "app.sessions.message.DisconnectSessions" /*+ REMEMBER_OPTION_HINT*/,
                    SessionInterruptionOption.ASK,
                    SessionInterruptionOption.IMMEDIATE,
                    SessionInterruptionOption.POST_TRANSACTION,
                    SessionInterruptionOption.CANCEL);

    private final InteractiveOptionBroker<SessionInterruptionOption> killSession =
            new InteractiveOptionBroker<>(
                    "kill-session",
                    "app.sessions.title.KillSessions",
                    "app.sessions.message.KillSessions"/* + REMEMBER_OPTION_HINT*/,
                    SessionInterruptionOption.ASK,
                    SessionInterruptionOption.NORMAL,
                    SessionInterruptionOption.IMMEDIATE,
                    SessionInterruptionOption.CANCEL);

    public SessionBrowserSettings(OperationSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.sessions.title.SessionBrowser");
    }

    @Override
    public String getHelpTopic() {
        return "sessionBrowser";
    }


    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public SessionBrowserSettingsForm createConfigurationEditor() {
        return new SessionBrowserSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "session-browser";
    }

    @Override
    public void readConfiguration(Element element) {
        disconnectSession.readConfiguration(element);
        killSession.readConfiguration(element);
        reloadOnFilterChange = Settings.getBoolean(element, "reload-on-filter-change", reloadOnFilterChange);
    }

    @Override
    public void writeConfiguration(Element element) {
        disconnectSession.writeConfiguration(element);
        killSession.writeConfiguration(element);
        Settings.setBoolean(element, "reload-on-filter-change", reloadOnFilterChange);
    }
}
