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
import org.jboss.pnc.dingrogu.api.dto.dummy.DummyServiceRequestDTO;

@ApplicationScoped
public class DummyClient {

    @Inject
    Tokens tokens;

    @Retry
    public void start(String dummyUrl, String callbackUrl) {
        Log.info("Sending dummy request to server: " + dummyUrl);

        DummyServiceRequestDTO request = DummyServiceRequestDTO.builder().callbackUrl(callbackUrl).build();
        HttpResponse<JsonNode> response = Unirest.post(dummyUrl)
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
}
