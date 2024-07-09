package com.dbn.error.jira;

import com.dbn.common.util.Commons;
import com.dbn.error.IssueReport;
import com.dbn.error.IssueReportBuilder;
import com.dbn.error.IssueReportSubmitter;
import com.dbn.error.TicketResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public class JiraIssueReportSubmitter extends IssueReportSubmitter {
    private static final HttpClientBuilder HTTP_CLIENT_BUILDER = HttpClientBuilder.create();
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();
    private static final JiraIssueReportBuilder REPORT_BUILDER = new JiraIssueReportBuilder();
    private static final String URL = "https://database-navigator.atlassian.net/";

    @Override
    public String getTicketUrl(String ticketId) {
        return URL + "browse/" + ticketId;
    }

    @Override
    protected IssueReportBuilder getBuilder() {
        return REPORT_BUILDER;
    }

    @NotNull
    @Override
    public TicketResponse submit(IssueReport report) throws Exception {
        JiraTicketRequest ticketRequest = new JiraTicketRequest(report);
        try {
            Gson gson = GSON_BUILDER.create();
            String requestString = gson.toJson(ticketRequest.getJsonObject());
            StringEntity params = new StringEntity(requestString);

            // https://developer.atlassian.com/cloud/jira/platform/jira-rest-api-basic-authentication/
            // dancioca@bluewin.ch:YeRDb1bt6dA7DJH75D8j3465 => BASE64
            // plugin@database-navigator.com:YeRDb1bt6dA7DJH75D8j3465 => BASE64

            // plugin@database-navigator.com:ATATT3xFfGF0QO9RzZWFEAU02eQcV7sfzory1FO0_3P6FL9U5PCJvNWebWG3t8Eb_jpT80AbeOL2etbex9R-EO_YFXabqI0oBmEYMusHdXXadOmlfSSCp6Cjh0vpAMYi60vofrXT58_2NDGCYWRaXwnDznIHqtcwLs8fX-nFwVp_N6rL_OUaf7s=A9A17623 => BASE64

            HttpPost httpPost = new HttpPost(URL + "rest/api/latest/issue");
            httpPost.addHeader("Authorization", "Basic cGx1Z2luQGRhdGFiYXNlLW5hdmlnYXRvci5jb206QVRBVFQzeEZmR0YwUU85UnpaV0ZFQVUwMmVRY1Y3c2Z6b3J5MUZPMF8zUDZGTDlVNVBDSnZOV2ViV0czdDhFYl9qcFQ4MEFiZU9MMmV0YmV4OVItRU9fWUZYYWJxSTBvQm1FWU11c0hkWFhhZE9tbGZTU0NwNkNqaDB2cEFNWWk2MHZvZnJYVDU4XzJOREdDWVdSYVh3bkR6bklIcXRjd0xzOGZYLW5Gd1ZwX042ckxfT1VhZjdzPUE5QTE3NjIz");
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(params);
            try (CloseableHttpClient httpClient = HTTP_CLIENT_BUILDER.build()) {
                HttpResponse httpResponse = httpClient.execute(httpPost);

                if (httpResponse == null) {
                    return new JiraTicketResponse(null, "Received empty response from server");
                } else {
                    InputStream in = httpResponse.getEntity().getContent();
                    String responseString = Commons.readInputStream(in);
                    return new JiraTicketResponse(responseString, null);
                }
            }
        } catch (Exception e) {
            conditionallyLog(e);
            return new JiraTicketResponse(null, e.getMessage());
        }
    }
}
