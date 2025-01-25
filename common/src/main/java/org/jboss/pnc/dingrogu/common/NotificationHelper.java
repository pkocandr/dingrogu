package org.jboss.pnc.dingrogu.common;

import org.jboss.pnc.rex.common.enums.State;
import org.jboss.pnc.rex.dto.TaskDTO;
import org.jboss.pnc.rex.model.requests.NotificationRequest;

import java.util.Collection;

public class NotificationHelper {

    /**
     * We want to capture a notificationRequest that went from running to a final state. We don't want something like
     * "Not really running" (like WAITING) to a final state since that task didn't do anything
     *
     * @param notificationRequest notificationRequest
     * @return
     */
    public static boolean isFromRunningToFinal(NotificationRequest notificationRequest) {
        State stateBefore = notificationRequest.getBefore();
        State stateAfter = notificationRequest.getAfter();

        if (stateBefore != State.UP && stateBefore != State.STARTING) {
            // we only care about UP -> something and STARTING -> something transition
            return false;
        }

        return stateAfter.isFinal();
    }

    public static boolean areAllRexTasksInFinalState(Collection<TaskDTO> tasks) {
        return tasks.stream().allMatch(task -> task.getState().isFinal());
    }
}
