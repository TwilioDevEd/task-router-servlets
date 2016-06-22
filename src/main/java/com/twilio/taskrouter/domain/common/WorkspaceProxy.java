package com.twilio.taskrouter.domain.common;

import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.TwilioTaskRouterClient;
import com.twilio.sdk.resource.instance.taskrouter.Activity;
import com.twilio.sdk.resource.instance.taskrouter.TaskQueue;
import com.twilio.sdk.resource.instance.taskrouter.Worker;
import com.twilio.sdk.resource.instance.taskrouter.Workflow;
import com.twilio.sdk.resource.instance.taskrouter.Workspace;
import com.twilio.sdk.taskrouter.WorkflowConfiguration;
import com.twilio.sdk.taskrouter.WorkflowRule;
import com.twilio.sdk.taskrouter.WorkflowRuleTarget;
import com.twilio.taskrouter.domain.error.TaskRouterException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

/**
 * Proxy for {@link com.twilio.sdk.resource.instance.taskrouter.Workspace}
 */
public final class WorkspaceProxy {

  private static final Logger LOG = Logger.getLogger(WorkspaceProxy.class.getName());

  private final TwilioTaskRouterClient taskRouterClient;

  private final Workspace workspace;

  private WorkspaceProxy(TwilioTaskRouterClient taskRouterClient, Workspace workspace) {
    this.taskRouterClient = taskRouterClient;
    this.workspace = workspace;
  }

  public static WorkspaceProxy createWorkspaceProxy(TwilioTaskRouterClient twilioTaskRouterClient,
                                                    Map<String, String> params) {
    String workspaceName = params.get("FriendlyName");
    StreamSupport.stream(twilioTaskRouterClient.getWorkspaces().spliterator(), false)
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
      return new WorkspaceProxy(twilioTaskRouterClient, workspace);
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
    return StreamSupport.stream(workspace.getActivities().spliterator(), false)
      .filter(activity -> activity.getFriendlyName().equals(activityName)).findFirst();
  }

  public Optional<TaskQueue> findTaskQueueByName(String queueName) {
    return StreamSupport.stream(workspace.getTaskQueues().spliterator(), false)
      .filter(task -> task.getFriendlyName().equals(queueName)).findFirst();
  }

  public Optional<Workflow> findWorkflowByName(String workflowName) {
    return StreamSupport.stream(workspace.getWorkflows().spliterator(), false)
      .filter(workflow -> workspace.getFriendlyName().equals(workflowName)).findFirst();
  }

  public void update(Map<String, String> params) {
    try {
      workspace.update(params);
    } catch (TwilioRestException e) {
      throw new TaskRouterException(String.format("The workspace %s(%s) couldnt be updated: %s",
        workspace.getFriendlyName(), workspace.getSid(), workspace.getSid()));
    }
  }

  public String createWorkFlowJsonConfig(JsonObject workflowJson) {
    try {
      JsonArray routingConfigRules = workflowJson.getJsonArray("routingConfiguration");
      TaskQueue defaultQueue = findTaskQueueByName("Default")
        .orElseThrow(() -> new TaskRouterException("Default queue not found"));
      WorkflowRuleTarget defaultTarget = new WorkflowRuleTarget(defaultQueue.getSid());
      List<WorkflowRule> rules = new LinkedList<>();
      routingConfigRules.getValuesAs(JsonObject.class).stream().forEach(ruleJson -> {
        String ruleQueueName = ruleJson.getString("targetTaskQueue");
        TaskQueue ruleQueue = findTaskQueueByName(ruleQueueName).orElseThrow(
          () -> new TaskRouterException(String.format("%s queue not found", ruleQueueName)));
        WorkflowRuleTarget ruleTarget = new WorkflowRuleTarget(ruleQueue.getSid());
        List<WorkflowRuleTarget> ruleTargets = Arrays.asList(ruleTarget, defaultTarget);
        rules.add(new WorkflowRule(ruleJson.getString("expression"), ruleTargets));
      });
      WorkflowConfiguration config = new WorkflowConfiguration(rules, defaultTarget);
      return config.toJSON();
    } catch (Exception ex) {
      throw new TaskRouterException("Error while creating workflow json configuration: "
        + ex.getMessage());
    }
  }
}
