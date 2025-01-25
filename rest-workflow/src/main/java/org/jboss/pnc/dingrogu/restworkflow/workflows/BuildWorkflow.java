package org.jboss.pnc.dingrogu.restworkflow.workflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.common.log.MDCUtils;
import org.jboss.pnc.dingrogu.api.dto.workflow.BuildWorkDTO;
import org.jboss.pnc.dingrogu.api.dto.CorrelationId;
import org.jboss.pnc.dingrogu.restadapter.adapter.RepositoryDriverPromoteAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.RepositoryDriverSealAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.RepositoryDriverSetupAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.RepourAdjustAdapter;
import org.jboss.pnc.dingrogu.restadapter.adapter.ReqourAdjustAdapter;
import org.jboss.pnc.rex.api.TaskEndpoint;
import org.jboss.pnc.rex.common.enums.State;
import org.jboss.pnc.rex.dto.ConfigurationDTO;
import org.jboss.pnc.rex.dto.CreateTaskDTO;
import org.jboss.pnc.rex.dto.EdgeDTO;
import org.jboss.pnc.rex.dto.TaskDTO;
import org.jboss.pnc.rex.dto.requests.CreateGraphRequest;
import org.jboss.pnc.rex.model.requests.NotificationRequest;
import org.jboss.pnc.rex.model.requests.StartRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Build process workflow implementation
 */
@ApplicationScoped
public class BuildWorkflow implements Workflow<BuildWorkDTO> {

    @Inject
    RepourAdjustAdapter repour;

    @Inject
    ReqourAdjustAdapter reqour;

    @Inject
    RepositoryDriverSetupAdapter repoSetup;

    @Inject
    RepositoryDriverSealAdapter repoSeal;

    @Inject
    RepositoryDriverPromoteAdapter repoPromote;

    @Inject
    TaskEndpoint taskEndpoint;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "dingrogu.url")
    public String ownUrl;

    @Override
    public CorrelationId submitWorkflow(BuildWorkDTO buildWorkDTO) throws WorkflowSubmissionException {
        CorrelationId correlationId = CorrelationId.generateUnique();

        try {
            CreateTaskDTO taskAlign = repour
                    .generateRexTask(ownUrl, correlationId.getId(), buildWorkDTO, buildWorkDTO.toRepourAdjustDTO());
            CreateTaskDTO taskAlignReqour = reqour
                    .generateRexTask(ownUrl, correlationId.getId(), buildWorkDTO, buildWorkDTO.toReqourAdjustDTO());
            CreateTaskDTO taskRepoSetup = repoSetup
                    .generateRexTask(ownUrl, correlationId.getId(), buildWorkDTO, buildWorkDTO.toRepositoryDriverSetupDTO());
            CreateTaskDTO taskRepoSeal = repoSeal
                    .generateRexTask(ownUrl, correlationId.getId(), buildWorkDTO, buildWorkDTO.toRepositoryDriverSealDTO());
            CreateTaskDTO taskRepoPromote = repoPromote
                    .generateRexTask(ownUrl, correlationId.getId(), buildWorkDTO, buildWorkDTO.toRepositoryDriverPromoteDTO());

            List<CreateTaskDTO> tasks = List.of(taskAlignReqour, taskRepoSetup, taskRepoSeal, taskRepoPromote);
            Map<String, CreateTaskDTO> vertices = getVertices(tasks);

            EdgeDTO alignToRepoSetup = EdgeDTO.builder()
                    .source(taskRepoSetup.name)
                    .target(taskAlignReqour.name)
                    .build();

            // Temporary: testing
            EdgeDTO repoSetupToRepoSeal = EdgeDTO.builder()
                    .source(taskRepoSeal.name)
                    .target(taskRepoSetup.name)
                    .build();
            EdgeDTO repoSealToRepoPromote = EdgeDTO.builder()
                    .source(taskRepoPromote.name)
                    .target(taskRepoSeal.name)
                    .build();

            Set<EdgeDTO> edges = Set.of(alignToRepoSetup, repoSetupToRepoSeal, repoSealToRepoPromote);

            ConfigurationDTO configurationDTO = ConfigurationDTO.builder()
                    .mdcHeaderKeyMapping(MDCUtils.HEADER_KEY_MAPPING)
                    .build();
            CreateGraphRequest graphRequest = new CreateGraphRequest(
                    correlationId.getId(),
                    configurationDTO,
                    edges,
                    vertices);
            taskEndpoint.start(graphRequest);

            return correlationId;

        } catch (Exception e) {
            throw new WorkflowSubmissionException(e);
        }
    }

    /**
     * Variant of submitWorkflow accepting the Rex startRequest
     *
     * @param startRequest
     * @return
     * @throws WorkflowSubmissionException
     */
    public CorrelationId submitWorkflow(StartRequest startRequest) throws WorkflowSubmissionException {
        BuildWorkDTO buildWorkDTO = objectMapper.convertValue(startRequest.getPayload(), BuildWorkDTO.class);
        CorrelationId correlationId = CorrelationId.generateUnique();

        try {
            CreateTaskDTO taskAlignReqour = reqour
                    .generateRexTask(ownUrl, correlationId.getId(), startRequest, buildWorkDTO.toReqourAdjustDTO());
            CreateTaskDTO taskRepoSetup = repoSetup.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    startRequest,
                    buildWorkDTO.toRepositoryDriverSetupDTO());
            CreateTaskDTO taskRepoSeal = repoSeal.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    startRequest,
                    buildWorkDTO.toRepositoryDriverSealDTO());
            CreateTaskDTO taskRepoPromote = repoPromote.generateRexTask(
                    ownUrl,
                    correlationId.getId(),
                    startRequest,
                    buildWorkDTO.toRepositoryDriverPromoteDTO());

            List<CreateTaskDTO> tasks = List.of(taskAlignReqour, taskRepoSetup, taskRepoSeal, taskRepoPromote);
            Map<String, CreateTaskDTO> vertices = getVertices(tasks);

            EdgeDTO alignToRepoSetup = EdgeDTO.builder()
                    .source(taskRepoSetup.name)
                    .target(taskAlignReqour.name)
                    .build();

            // Temporary: testing
            EdgeDTO repoSetupToRepoSeal = EdgeDTO.builder()
                    .source(taskRepoSeal.name)
                    .target(taskRepoSetup.name)
                    .build();
            EdgeDTO repoSealToRepoPromote = EdgeDTO.builder()
                    .source(taskRepoPromote.name)
                    .target(taskRepoSeal.name)
                    .build();

            Set<EdgeDTO> edges = Set.of(alignToRepoSetup, repoSetupToRepoSeal, repoSealToRepoPromote);

            ConfigurationDTO configurationDTO = ConfigurationDTO.builder()
                    .mdcHeaderKeyMapping(MDCUtils.HEADER_KEY_MAPPING)
                    .build();
            CreateGraphRequest graphRequest = new CreateGraphRequest(
                    correlationId.getId(),
                    configurationDTO,
                    edges,
                    vertices);
            taskEndpoint.start(graphRequest);

            return correlationId;

        } catch (Exception e) {
            throw new WorkflowSubmissionException(e);
        }
    }

    @Override
    public Response rexNotification(NotificationRequest notificationRequest) {
        State stateBefore = notificationRequest.getBefore();
        State stateAfter = notificationRequest.getAfter();

        if (stateBefore != State.UP && stateBefore != State.STARTING) {
            // we only care about UP -> something and STARTING -> something transition
            return Response.ok().build();
        }

        if (!stateAfter.isFinal()) {
            // we don't care about that statue
            return Response.ok().build();
        }

        Log.infof("[%s] -> [%s] :: %s", stateBefore, stateAfter, notificationRequest.getTask().getName());

        String correlationId = notificationRequest.getTask().getCorrelationID();
        Set<TaskDTO> tasks = taskEndpoint.byCorrelation(correlationId);

        if (areAllRexTasksInFinalState(tasks)) {
            // TODO
            // assemble the final buildresult object from task results
            // send that buildresult object to sender
            Log.infof("Right now I should be sending a notification to the caller");
            tasks.forEach(taskDTO -> Log.infof("Task: %s, state: %s", taskDTO.getName(), taskDTO.getState()));

            // we set the notification attachment to be the StartRequest in submitWorkflow method
            StartRequest request = objectMapper.convertValue(notificationRequest.getAttachment(), StartRequest.class);
            Log.infof("request is: %s", request);
            // from start request I get the positive callback and negative callback + initial request for the build
            // workflow
        }
        return Response.ok().build();
    }

    private boolean areAllRexTasksInFinalState(Set<TaskDTO> tasks) {
        boolean anyNonFinalState = tasks.stream().anyMatch(task -> !task.getState().isFinal());
        return !anyNonFinalState;
    }

    private static Map<String, CreateTaskDTO> getVertices(List<CreateTaskDTO> tasks) {
        Map<String, CreateTaskDTO> vertices = new HashMap<>();
        for (CreateTaskDTO task : tasks) {
            vertices.put(task.name, task);
        }
        return vertices;
    }
}
