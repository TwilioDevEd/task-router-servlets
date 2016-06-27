package com.twilio.taskrouter.domain.model;

import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.TwilioTaskRouterClient;
import com.twilio.sdk.resource.instance.taskrouter.Activity;
import com.twilio.sdk.resource.instance.taskrouter.TaskQueue;
import com.twilio.sdk.resource.instance.taskrouter.Worker;
import com.twilio.sdk.resource.instance.taskrouter.Workflow;
import com.twilio.sdk.resource.instance.taskrouter.Workspace;
import com.twilio.taskrouter.domain.error.TaskRouterException;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Proxy for {@link com.twilio.sdk.resource.instance.taskrouter.Workspace}
 */
public final class WorkspaceFacade {

  private static final Logger LOG = Logger.getLogger(WorkspaceFacade.class.getName());

  private final TwilioTaskRouterClient taskRouterClient;

  private final Workspace workspace;

  private WorkspaceFacade(TwilioTaskRouterClient taskRouterClient, Workspace workspace) {
    this.taskRouterClient = taskRouterClient;
    this.workspace = workspace;
  }

  public static WorkspaceFacade create(TwilioTaskRouterClient twilioTaskRouterClient,
                                       Map<String, String> params) {
    String workspaceName = params.get("FriendlyName");
    twilioTaskRouterClient.getWorkspaces().getPageData().stream()
      .filter(workspace -> workspace.getFriendlyName().equals(workspaceName)).findFirst()
      .ifPresent(workspace -> {
        try {
          twilioTaskRouterClient.deleteWorkspace(workspace.getSid());
        } catch (TwilioRestException e) {
          throw new TaskRouterException(String.format("Error deleting existing workspace '%s': %s",
            workspaceName, e.getMessage()));
        }
      });
    try {
      Workspace workspace = twilioTaskRouterClient.createWorkspace(params);
      return new WorkspaceFacade(twilioTaskRouterClient, workspace);
    } catch (TwilioRestException e) {
      throw new TaskRouterException(String.format("Error creating new workspace '%s': %s",
        workspaceName, e.getMessage()));
    }
  }

  public String getFriendlyName() {
    return workspace.getFriendlyName();
  }

  public Worker addWorker(Map<String, String> parameters) {
    try {

      return workspace.createWorker(parameters);
    } catch (TwilioRestException e) {
      throw new TaskRouterException(String.format(
        "Error while adding the worker '%s' to workspace: %s",
        parameters.get("FriendlyName"), e.getMessage()));
    }
  }

  public void addTaskQueue(Map<String, String> properties) {
    try {
      taskRouterClient.createTaskQueue(workspace.getSid(), properties);
    } catch (TwilioRestException e) {
      throw new TaskRouterException(String.format(
        "Error while adding the task queue '%s' to workspace: %s",
        properties.get("FriendlyName"), e.getMessage()));
    }
  }

  public Workflow addWorkflow(Map<String, String> properties) {
    try {
      return taskRouterClient.createWorkflow(workspace.getSid(), properties);
    } catch (TwilioRestException e) {
      throw new TaskRouterException(String.format(
        "Error while adding workflow '%s' to workspace: %s",
        workspace.getFriendlyName(), e.getMessage()));
    }
  }

  public Optional<Activity> findActivityByName(String activityName) {
    return workspace.getActivities().getPageData().stream()
      .filter(activity -> activity.getFriendlyName().equals(activityName)).findFirst();
  }

  public Optional<TaskQueue> findTaskQueueByName(String queueName) {
    return workspace.getTaskQueues().getPageData().stream()
      .filter(task -> task.getFriendlyName().equals(queueName)).findFirst();
  }

  public Optional<Workflow> findWorkflowByName(String workflowName) {
    return workspace.getWorkflows().getPageData().stream()
      .filter(workflow -> workspace.getFriendlyName().equals(workflowName)).findFirst();
  }
}
