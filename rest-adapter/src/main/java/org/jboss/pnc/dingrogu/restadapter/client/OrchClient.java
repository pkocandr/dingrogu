package org.jboss.pnc.dingrogu.restadapter.client;

import io.quarkus.logging.Log;
import io.quarkus.oidc.client.Tokens;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import org.jboss.pnc.api.deliverablesanalyzer.dto.AnalysisResult;

@ApplicationScoped
public class OrchClient {

    @Inject
    Tokens tokens;

    // TODO
    public void submitDelAResult(String orchUrl, AnalysisResult result) {

        Log.info("Sending dela response to server: " + orchUrl);

        HttpResponse<JsonNode> response = Unirest.post(orchUrl + "/pnc-rest/v2/deliverable-analyses/complete")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .header("Accept", "application/json")
                .body(result)
                .asJson();

        if (!response.isSuccess()) {
            Log.info(response.getStatus());
            Log.info(response.getStatusText());
            Log.info(response.getBody().toPrettyString());
            throw new RuntimeException("Request didn't go through");
        }
    }
}
