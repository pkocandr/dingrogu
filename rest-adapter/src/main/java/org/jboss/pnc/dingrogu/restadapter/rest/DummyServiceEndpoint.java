package org.jboss.pnc.dingrogu.restadapter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.dingrogu.api.dto.dummy.DummyServiceRequestDTO;
import org.jboss.pnc.dingrogu.api.dto.dummy.DummyServiceResponseDTO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/dummy-service")
public class DummyServiceEndpoint {

    ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Inject
    ObjectMapper objectMapper;

    @POST
    public void startDummyService(DummyServiceRequestDTO dto) {

        Log.info("in dummy service");
        executorService.submit(() -> {
            Log.info("Start of executor service");
            // Sleep so that we run this code after we return success
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
            }

            DummyServiceResponseDTO reply = DummyServiceResponseDTO.builder().status("OK").build();
            Log.info("Sending callback to: " + dto.getCallbackUrl());
            HttpResponse<JsonNode> response = Unirest.post(
                    dto.getCallbackUrl())
                    .header("Content-Type", "application/json").header("Accept", "application/json")
                    .body(reply).asJson();
            Log.info("Dummy response sent");

            Log.info(response.getBody().toPrettyString());
            Log.info(response.getStatus());
        });
    }
}
