package com.twilio.taskrouter.domain.model;

import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.TwilioTaskRouterClient;
import com.twilio.sdk.resource.instance.taskrouter.Activity;
import com.twilio.sdk.resource.instance.taskrouter.TaskQueue;
import com.twilio.sdk.resource.instance.taskrouter.Worker;
import com.twilio.sdk.resource.instance.taskrouter.Workflow;
import com.twilio.sdk.resource.instance.taskrouter.Workspace;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Facade pattern for {@link com.twilio.sdk.resource.instance.taskrouter.Workspace}
 */
public class WorkspaceFacade {

  private final TwilioTaskRouterClient taskRouterClient;

  private final Workspace workspace;

  private Activity idleActivity;

  private Map<String, Worker> phoneToWorker;

  public WorkspaceFacade(TwilioTaskRouterClient taskRouterClient, Workspace workspace) {
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

  public static Optional<WorkspaceFacade> findBySid(String workspaceSid, TwilioTaskRouterClient
    twilioTaskRouterClient) {
    return twilioTaskRouterClient.getWorkspaces().getPageData().stream()
      .filter(workspace -> workspace.getSid().equals(workspaceSid)).findFirst()
      .map(workspace -> new WorkspaceFacade(twilioTaskRouterClient, workspace));
  }

  public String getFriendlyName() {
    return workspace.getFriendlyName();
  }

  public String getSid() {
    return workspace.getSid();
  }

  public Worker addWorker(Map<String, String> workerParams) {
    try {
      return workspace.createWorker(workerParams);
    } catch (TwilioRestException e) {
      throw new TaskRouterException(String.format(
        "Error while adding the worker '%s' to workspace: %s",
        workerParams.get("FriendlyName"), e.getMessage()));
    }
  }

  public void addTaskQueue(Map<String, String> taskQueueParams) {
    try {
      taskRouterClient.createTaskQueue(workspace.getSid(), taskQueueParams);
    } catch (TwilioRestException e) {
      throw new TaskRouterException(String.format(
        "Error while adding the task queue '%s' to workspace: %s",
        taskQueueParams.get("FriendlyName"), e.getMessage()));
    }
  }

  public Workflow addWorkflow(Map<String, String> workflowParams) {
    try {
      return taskRouterClient.createWorkflow(workspace.getSid(), workflowParams);
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

  public Optional<Worker> findWorkerByPhone(String workerPhone) {
    return Optional.ofNullable(getPhoneToWorker().get(workerPhone));
  }

  public Map<String, Worker> getPhoneToWorker() {
    if (phoneToWorker == null) {
      phoneToWorker = new HashMap<>();
      workspace.getWorkers().getPageData().stream().forEach(worker -> {
        try {
          Map<String, Object> attributes = worker.parseAttributes();
          phoneToWorker.put(attributes.get("contact_uri").toString(), worker);
        } catch (ParseException e) {
          throw new TaskRouterException(
            String.format("'%s' has a malformed json attributes", worker.getFriendlyName()));
        }
      });
    }
    return phoneToWorker;
  }

  public Activity getIdleActivity() {
    if (idleActivity == null) {
      idleActivity = findActivityByName("Idle").get();
    }
    return idleActivity;
  }
}
