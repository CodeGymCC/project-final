package com.codegym.jira.bugtracking.task;

import com.codegym.jira.common.error.DataConflictException;
import com.codegym.jira.login.AuthUser;
import com.codegym.jira.ref.RefTo;
import com.codegym.jira.ref.RefType;
import com.codegym.jira.ref.ReferenceService;
import com.codegym.jira.bugtracking.task.to.TaskToExt;
import com.codegym.jira.bugtracking.task.to.TaskToFull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TaskUtil {

    static Map<String, RefTo> getPossibleStatusRefs(String currentStatus) {
        Set<String> possibleStatuses = getPossibleStatuses(currentStatus);
        return ReferenceService.getRefs(RefType.TASK_STATUS).entrySet().stream()
                .filter(ref -> possibleStatuses.contains(ref.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (ref1, ref2) -> ref1, LinkedHashMap::new));
    }

    static void checkStatusChangePossible(String currentStatus, String newStatus) {
        if (!getPossibleStatuses(currentStatus).contains(newStatus)) {
            throw new DataConflictException("Cannot change task status from " + currentStatus + " to " + newStatus);
        }
    }

    private static Set<String> getPossibleStatuses(String currentStatus) {
        Set<String> possibleStatuses = new HashSet<>();
        possibleStatuses.add(currentStatus);
        Map<String, RefTo> taskStatusRefs = ReferenceService.getRefs(RefType.TASK_STATUS);
        String aux = taskStatusRefs.get(currentStatus).getAux(0);
        possibleStatuses.addAll(aux == null ? Set.of() : Set.of(aux.split(",")));
        return possibleStatuses;
    }

    static void fillExtraFields(TaskToFull taskToFull, List<Activity> activities) {
        if (!activities.isEmpty()) {
            taskToFull.setUpdated(activities.get(0).getUpdated());
            for (Activity latest : activities) {
                if (taskToFull.getDescription() == null && latest.getDescription() != null) {
                    taskToFull.setDescription(latest.getDescription());
                }
                if (taskToFull.getPriorityCode() == null && latest.getPriorityCode() != null) {
                    taskToFull.setPriorityCode(latest.getPriorityCode());
                }
                if (taskToFull.getEstimate() == null && latest.getEstimate() != null) {
                    taskToFull.setEstimate(latest.getEstimate());
                }
                if (taskToFull.getDescription() != null && taskToFull.getPriorityCode() != null && taskToFull.getEstimate() != null)
                    break;
            }
        }
    }

    static String getLatestValue(List<Activity> activities, Function<Activity, String> valueExtractFunction) {
        for (Activity activity : activities) {
            String value = valueExtractFunction.apply(activity);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    static Activity makeActivity(long taskId, TaskToExt taskTo) {
        return new Activity(null, taskId, AuthUser.authId(), null, null, taskTo.getStatusCode(), taskTo.getPriorityCode(),
                taskTo.getTypeCode(), taskTo.getTitle(), taskTo.getDescription(), taskTo.getEstimate());
    }
}
