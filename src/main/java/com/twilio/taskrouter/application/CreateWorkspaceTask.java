package com.twilio.taskrouter.application;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.twilio.sdk.resource.instance.taskrouter.Activity;
import com.twilio.sdk.resource.instance.taskrouter.Workflow;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.common.Utils;
import com.twilio.taskrouter.domain.common.WorkspaceProxy;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import org.apache.commons.lang3.StringUtils;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * Creates a workspace
 */
class CreateWorkspaceTask {

  private static final Logger LOG = Logger.getLogger(CreateWorkspaceTask.class.getName());

  public static void main(String[] args) {
    System.out.println("Creating workspace...");
    if (args.length < 3) {
      System.out.println("You must specify 3 parameters:");
      System.out.println("- Server hostname. E.g, <hash>.ngrok.com");
      System.out.println("- Phone of the first agent (Bob)");
      System.out.println("- Phone of the secondary agent (Alice)");
      exit(1);
    }
    String hostname = args[0];
    String bobPhone = args[1];
    String alicePhone = args[2];
    System.out.println(String.format("server: %s\nBob phone: %s\nAlice phone: %s\n",
      hostname, bobPhone, alicePhone));
    Injector injector = Guice.createInjector();
    final TwilioAppSettings twilioSettings = injector.getInstance(TwilioAppSettings.class);
    //Get the configuration
    JsonObject workspaceConfig = twilioSettings.createWorkspaceConfig(args);
    //Get or Create the Workspace
    Map<String, String> params = new HashMap<>();
    String workspaceName = workspaceConfig.getString("name");
    params.put("FriendlyName", workspaceName);
    params.put("EventCallbackUrl", workspaceConfig.getString("event_callback"));
    try {
      WorkspaceProxy workspaceProxy = WorkspaceProxy
        .createWorkspaceProxy(twilioSettings.getTwilioTaskRouterClient(), params);
      Activity idleActivity = workspaceProxy.findActivityByName("Idle").orElseThrow(() ->
        new TaskRouterException("The activity 'Idle' was not found to be set for Timeout."));
      params.clear();
      params.put("TimeoutActivitySid", idleActivity.getSid());
      workspaceProxy.update(params);
      addWorkersToWorkspace(workspaceProxy, workspaceConfig);
      addTaskQueuesToWorkspace(workspaceProxy, workspaceConfig);
      Workflow workflow = addWorkflowToWorkspace(workspaceProxy, workspaceConfig);
      printSuccessAndInstructions(workspaceProxy, workflow);
    } catch (TaskRouterException e) {
      LOG.severe(e.getMessage());
      exit(1);
    }
  }

  public static void addWorkersToWorkspace(WorkspaceProxy workspaceProxy,
                                           JsonObject workspaceJsonConfig) {
    JsonArray workersJson = workspaceJsonConfig.getJsonArray("workers");
    Activity idleActivity = workspaceProxy.findActivityByName("Idle").orElseThrow(() ->
      new TaskRouterException("The activity 'Idle' was not found. Workers cannot be added"));
    workersJson.getValuesAs(JsonObject.class).forEach(workerJson -> {
      Map<String, String> attributes = new HashMap<>();
      attributes.put("FriendlyName", workerJson.getString("name"));
      attributes.put("ActivitySid", idleActivity.getSid());
      attributes.put("Attributes", workerJson.getJsonObject("attributes").toString());
      try {
        workspaceProxy.addWorker(attributes);
      } catch (TaskRouterException e) {
        LOG.warning(e.getMessage());
      }
    });
  }

  public static void addTaskQueuesToWorkspace(WorkspaceProxy workspaceProxy,
                                              JsonObject workspaceJsonConfig) {
    JsonArray taskQueuesJson = workspaceJsonConfig.getJsonArray("task_queues");
    Activity reservationActivity = workspaceProxy.findActivityByName("Reserved").orElseThrow(() ->
      new TaskRouterException("The activity for reservations 'Reserved' was not found. "
        + "TaskQueues cannot be added."));
    Activity assignmentActivity = workspaceProxy.findActivityByName("Busy").orElseThrow(() ->
      new TaskRouterException("The activity for assignments 'Busy' was not found. "
        + "TaskQueues cannot be added."));
    taskQueuesJson.getValuesAs(JsonObject.class).forEach(taskQueueJson -> {
      Map<String, String> params = new HashMap<>();
      params.put("FriendlyName", taskQueueJson.getString("name"));
      params.put("ReservationActivitySid", reservationActivity.getSid());
      params.put("AssignmentActivitySid", assignmentActivity.getSid());
      params.put("TargetWorkers", taskQueueJson.getString("targetWorkers"));
      try {
        workspaceProxy.addTaskQueue(params);
      } catch (TaskRouterException e) {
        LOG.warning(e.getMessage());
      }
    });
  }

  public static Workflow addWorkflowToWorkspace(WorkspaceProxy workspaceProxy,
                                                JsonObject workspaceConfig) {
    JsonObject workflowJson = workspaceConfig.getJsonObject("workflow");
    String workflowName = workflowJson.getString("name");
    return workspaceProxy.findWorkflowByName(workflowName)
      .orElseGet(() -> {
        Map<String, String> properties = new HashMap<>();
        properties.put("FriendlyName", workflowName);
        properties.put("AssignmentCallbackUrl", workflowJson.getString("callback"));
        properties.put("FallbackAssignmentCallbackUrl", workflowJson.getString("callback"));
        properties.put("TaskReservationTimeout", workflowJson.getString("timeout"));
        String jsonWorkflowConfig = workspaceProxy.createWorkFlowJsonConfig(workflowJson);
        properties.put("Configuration", jsonWorkflowConfig);
        return workspaceProxy.addWorkflow(properties);
      });
  }

  public static void printSuccessAndInstructions(WorkspaceProxy workspaceProxy,
                                                 Workflow workflow) {
    Activity idleActivity = workspaceProxy.findActivityByName("Idle")
      .orElseThrow(() -> new TaskRouterException("The IDLE activity does not exist."));
    StringBuilder exportVarsCmdStrBuilder = new StringBuilder(String.format(
      "\nexport WORKFLOW_SID=%s\n", workflow.getSid()));
    exportVarsCmdStrBuilder.append(String.format("export POST_WORK_ACTIVITY_SID=%s\n",
      idleActivity.getSid()));
    String successMsg = String.format("Workspace '%s' was created successfully.",
      workspaceProxy.getFriendlyName());
    final int lineLength = successMsg.length() + 2;
    System.out.println(StringUtils.repeat("#", lineLength));
    System.out.println(String.format(" %s ", successMsg));
    System.out.println(StringUtils.repeat("#", lineLength));
    System.out.println("You have to set the following environment vars:");
    System.out.println(exportVarsCmdStrBuilder.toString());
    try {
      Utils.copyTextToClipboad(exportVarsCmdStrBuilder.toString());
      System.out.println("(Copied to your clipboard)");
    } catch (Throwable err) {
      LOG.fine("Could not copy commands to export variables into the clipboard");
    }
    System.out.println(StringUtils.repeat("#", lineLength));
  }
}
