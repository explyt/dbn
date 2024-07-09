package com.dbn.error.jira;

import com.dbn.error.IssueReport;
import com.dbn.error.TicketRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
class JiraTicketRequest implements TicketRequest {
    private final JsonObject jsonObject = new JsonObject();

    JiraTicketRequest(IssueReport report) {
        String summary = report.getSummary();
        summary = summary.replace("\r\n", " ").replace("\t", " ");

        // project
        JsonObject project = new JsonObject();
        project.addProperty("key", "DBNE");

        // issue type
        JsonObject issueType = new JsonObject();
        issueType.addProperty("name", "Exception");

        JsonArray versions = new JsonArray();
        JsonObject version = new JsonObject();
        version.addProperty("id", report.getPluginVersion());
        versions.add(version);



        // fields
        JsonObject fields = new JsonObject();
        fields.add("project", project);
        fields.addProperty("summary", summary);
        fields.addProperty("description", report.getDescription());
        fields.addProperty("environment", report.getIdeVersion());
        fields.add("issuetype", issueType);
        //fields.add("versions", versions); TODO create versions on the fly
        jsonObject.add("fields", fields);
    }
}
