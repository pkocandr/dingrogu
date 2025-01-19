package org.jboss.pnc.dingrogu.restworkflow.workflows;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dingrogu.api.client.RexClient;
import org.jboss.pnc.dingrogu.api.dto.CorrelationId;
import org.jboss.pnc.dingrogu.api.dto.adapter.DummyDTO;
import org.jboss.pnc.dingrogu.api.dto.workflow.DummyWorkflowDTO;
import org.jboss.pnc.dingrogu.restadapter.adapter.DummyAdapter;
import org.jboss.pnc.rex.dto.ConfigurationDTO;
import org.jboss.pnc.rex.dto.CreateTaskDTO;
import org.jboss.pnc.rex.dto.EdgeDTO;
import org.jboss.pnc.rex.dto.requests.CreateGraphRequest;

import java.util.Map;
import java.util.Set;

/**
 * Just a dummy workflow to test functionality with Rex and validate ideas
 */
@ApplicationScoped
@Slf4j
public class DummyWorkflow implements Workflow<DummyWorkflowDTO> {

    @Inject
    DummyAdapter dummyAdapter;

    @Inject
    RexClient rexClient;

    @ConfigProperty(name = "dingrogu.url")
    public String ownUrl;

    @Override
    public CorrelationId submitWorkflow(DummyWorkflowDTO dummyWorkflowDTO) throws WorkflowSubmissionException {
        CorrelationId correlationId = CorrelationId.generateUnique();

        Map<String, String> mdcMap = MDCUtils.getHeadersFromMDC();
        for (String key : mdcMap.keySet()) {
            log.info("Inside dummy workflow -> {}::{}", key, mdcMap.get(key));
        }
        DummyDTO dummyDTO = DummyDTO.builder().dummyServiceUrl(ownUrl + "/dummy-service").build();
        try {
            CreateTaskDTO task = dummyAdapter.generateRexTask(ownUrl, correlationId.getId(), dummyDTO);

            Map<String, CreateTaskDTO> vertices = Map.of(task.name, task);

            // EdgeDTO edgeDTO = EdgeDTO.builder().source(task.name).target(null).build();
            // Set<EdgeDTO> edges = Set.of(edgeDTO);
            Set<EdgeDTO> edges = Set.of();

            ConfigurationDTO configurationDTO = ConfigurationDTO.builder()
                    .mdcHeaderKeyMapping(org.jboss.pnc.common.log.MDCUtils.HEADER_KEY_MAPPING)
                    .build();
            CreateGraphRequest graphRequest = new CreateGraphRequest(
                    correlationId.getId(),
                    configurationDTO,
                    edges,
                    vertices);
            rexClient.submitWorkflow(graphRequest);

            return correlationId;

        } catch (Exception e) {
            throw new WorkflowSubmissionException(e);
        }
    }
}
