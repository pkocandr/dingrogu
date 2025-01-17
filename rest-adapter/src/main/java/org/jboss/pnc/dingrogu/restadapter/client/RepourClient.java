package org.jboss.pnc.dingrogu.restadapter.client;

import io.quarkus.logging.Log;
import io.quarkus.oidc.client.Tokens;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kong.unirest.core.ContentType;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.pnc.api.repour.dto.RepourAdjustRequest;
import org.jboss.pnc.api.repour.dto.RepourCloneRepositoryRequest;
import org.jboss.pnc.api.repour.dto.RepourCreateRepositoryRequest;
import org.jboss.pnc.dingrogu.api.dto.adapter.RepourCreateRepoResponse;

@ApplicationScoped
public class RepourClient {

    @Inject
    Tokens tokens;

    /**
     * TODO: processContext is part of the MDC values. We're just hardcoding it to the header for now until we work on
     * the MDC values
     * 
     * @param repourUrl
     * @param request
     */
    @Retry
    public void adjust(String repourUrl, RepourAdjustRequest request) {

        HttpResponse<JsonNode> response = Unirest.post(repourUrl + "/adjust")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(tokens))
                .body(request)
                .asJson();

        if (!response.isSuccess()) {
            Log.info(response.getStatus());
            Log.info(response.getStatusText());
            Log.info(response.getBody().toPrettyString());
            throw new RuntimeException("Request didn't go through");
        }
    }

    @Retry
    public void cloneRequest(String repourUrl, RepourCloneRepositoryRequest request) {

        HttpResponse<JsonNode> response = Unirest.post(repourUrl + "/clone")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(tokens))
                .body(request)
                .asJson();

        if (!response.isSuccess()) {
            Log.info(response.getStatus());
            Log.info(response.getStatusText());
            Log.info(response.getBody().toPrettyString());
            throw new RuntimeException("Request didn't go through");
        }
    }

    @Retry
    public RepourCreateRepoResponse createRepository(String repourUrl, RepourCreateRepositoryRequest request) {

        HttpResponse<RepourCreateRepoResponse> response = Unirest.post(repourUrl + "/clone")
                .contentType(ContentType.APPLICATION_JSON)
                .accept(ContentType.APPLICATION_JSON)
                .headers(ClientHelper.getClientHeaders(tokens))
                .body(request)
                .asObject(RepourCreateRepoResponse.class);

        if (!response.isSuccess()) {
            Log.info(response.getStatus());
            Log.info(response.getStatusText());
            Log.info(response.getBody());
            throw new RuntimeException("Request didn't go through");
        }

        return response.getBody();
    }
}
