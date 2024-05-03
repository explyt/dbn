package com.dbn.error;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import lombok.Data;

import static com.dbn.common.util.Commons.nvl;

@Data
public class IssueReport {
    private final Project project;
    private final IdeaPluginDescriptor plugin;
    private final IdeaLoggingEvent[] events;
    private final String message;
    private final Consumer<SubmittedReportInfo> consumer;

    private String osVersion;
    private String ideVersion;
    private String javaVersion;
    private String pluginVersion;


    private String databaseType;
    private String databaseName;
    private String databaseVersion;
    private String databaseDriver;

    private String summary;
    private String description;
    private String clientId;
    
    public IssueReport(
            Project project,
            IdeaPluginDescriptor plugin,
            IdeaLoggingEvent[] events,
            String message,
            Consumer<SubmittedReportInfo> consumer) {
        this.project = project;
        this.plugin = plugin;
        this.events = events;
        this.message = message;
        this.consumer = consumer;
    }

    public IdeaLoggingEvent getEvent() {
        return events[0];
    }

    public String getDatabaseType() {
        return nvl(databaseType, "NA");
    }

    public String getDatabaseName() {
        return nvl(databaseName, "NA");
    }

    public String getDatabaseVersion() {
        return nvl(databaseVersion, "NA");
    }

    public String getDatabaseDriver() {
        return nvl(databaseDriver, "NA");
    }

    public String getLastActionId() {
        return nvl(IdeaLogger.ourLastActionId, "NA");
    }
}
