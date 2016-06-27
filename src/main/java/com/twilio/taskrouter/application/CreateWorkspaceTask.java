package com.twilio.taskrouter.application;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.twilio.sdk.resource.instance.taskrouter.Activity;
import com.twilio.sdk.resource.instance.taskrouter.TaskQueue;
import com.twilio.sdk.resource.instance.taskrouter.Workflow;
import com.twilio.sdk.taskrouter.WorkflowConfiguration;
import com.twilio.sdk.taskrouter.WorkflowRule;
import com.twilio.sdk.taskrouter.WorkflowRuleTarget;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.common.Utils;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import com.twilio.taskrouter.domain.model.WorkspaceFacade;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    System.out.println(String.format("Server: %s\nBob phone: %s\nAlice phone: %s\n",
      hostname, bobPhone, alicePhone));
    //Get the configuration
    JsonObject workspaceConfig = createWorkspaceConfig(args);
    //Get or Create the Workspace
    Injector injector = Guice.createInjector();
    final TwilioAppSettings twilioSettings = injector.getInstance(TwilioAppSettings.class);
    Map<String, String> params = new HashMap<>();
    String workspaceName = workspaceConfig.getString("name");
    params.put("FriendlyName", workspaceName);
    params.put("EventCallbackUrl", workspaceConfig.getString("event_callback"));
    try {
      WorkspaceFacade workspaceFacade = WorkspaceFacade
        .create(twilioSettings.getTwilioTaskRouterClient(), params);
      addWorkersToWorkspace(workspaceFacade, workspaceConfig);
      addTaskQueuesToWorkspace(workspaceFacade, workspaceConfig);
      Workflow workflow = addWorkflowToWorkspace(workspaceFacade, workspaceConfig);
      printSuccessAndInstructions(workspaceFacade, workflow);
    } catch (TaskRouterException e) {
      LOG.severe(e.getMessage());
      exit(1);
    }
  }

  public static void addWorkersToWorkspace(WorkspaceFacade workspaceFacade,
                                           JsonObject workspaceJsonConfig) {
    JsonArray workersJson = workspaceJsonConfig.getJsonArray("workers");
    Activity idleActivity = workspaceFacade.findActivityByName("Idle").orElseThrow(() ->
      new TaskRouterException("The activity 'Idle' was not found. Workers cannot be added"));
    workersJson.getValuesAs(JsonObject.class).forEach(workerJson -> {
      Map<String, String> attributes = new HashMap<>();
      attributes.put("FriendlyName", workerJson.getString("name"));
      attributes.put("ActivitySid", idleActivity.getSid());
      attributes.put("Attributes", workerJson.getJsonObject("attributes").toString());
      try {
        workspaceFacade.addWorker(attributes);
      } catch (TaskRouterException e) {
        LOG.warning(e.getMessage());
      }
    });
  }

  public static void addTaskQueuesToWorkspace(WorkspaceFacade workspaceFacade,
                                              JsonObject workspaceJsonConfig) {
    JsonArray taskQueuesJson = workspaceJsonConfig.getJsonArray("task_queues");
    Activity reservationActivity = workspaceFacade.findActivityByName("Reserved").orElseThrow(() ->
      new TaskRouterException("The activity for reservations 'Reserved' was not found. "
        + "TaskQueues cannot be added."));
    Activity assignmentActivity = workspaceFacade.findActivityByName("Busy").orElseThrow(() ->
      new TaskRouterException("The activity for assignments 'Busy' was not found. "
        + "TaskQueues cannot be added."));
    taskQueuesJson.getValuesAs(JsonObject.class).forEach(taskQueueJson -> {
      Map<String, String> params = new HashMap<>();
      params.put("FriendlyName", taskQueueJson.getString("name"));
      params.put("TargetWorkers", taskQueueJson.getString("targetWorkers"));
      params.put("ReservationActivitySid", reservationActivity.getSid());
      params.put("AssignmentActivitySid", assignmentActivity.getSid());
      try {
        workspaceFacade.addTaskQueue(params);
      } catch (TaskRouterException e) {
        LOG.warning(e.getMessage());
      }
    });
  }

  public static Workflow addWorkflowToWorkspace(WorkspaceFacade workspaceFacade,
                                                JsonObject workspaceConfig) {
    JsonObject workflowJson = workspaceConfig.getJsonObject("workflow");
    String workflowName = workflowJson.getString("name");
    return workspaceFacade.findWorkflowByName(workflowName)
      .orElseGet(() -> {
        Map<String, String> properties = new HashMap<>();
        properties.put("FriendlyName", workflowName);
        properties.put("AssignmentCallbackUrl", workflowJson.getString("callback"));
        properties.put("FallbackAssignmentCallbackUrl", workflowJson.getString("callback"));
        properties.put("TaskReservationTimeout", workflowJson.getString("timeout"));
        String jsonWorkflowConfig = createWorkFlowJsonConfig(workspaceFacade, workflowJson);
        properties.put("Configuration", jsonWorkflowConfig);
        return workspaceFacade.addWorkflow(properties);
      });
  }

  public static void printSuccessAndInstructions(WorkspaceFacade workspaceFacade,
                                                 Workflow workflow) {
    Activity idleActivity = workspaceFacade.findActivityByName("Idle")
      .orElseThrow(() -> new TaskRouterException("The IDLE activity does not exist."));
    StringBuilder exportVarsCmdStrBuilder = new StringBuilder(String.format(
      "\nexport WORKFLOW_SID=%s\n", workflow.getSid()));
    exportVarsCmdStrBuilder.append(String.format("export POST_WORK_ACTIVITY_SID=%s\n",
      idleActivity.getSid()));
    String successMsg = String.format("Workspace '%s' was created successfully.",
      workspaceFacade.getFriendlyName());
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

  public static JsonObject createWorkspaceConfig(String[] args) {
    final String configFileName = "workspace.json";
    Optional<URL> url =
      Optional.ofNullable(CreateWorkspaceTask.class.getResource(File.separator + configFileName));
    return url.map(u -> {
      try {
        File workspaceConfigJsonFile = new File(u.toURI());
        String jsonContent = Utils.readFileContent(workspaceConfigJsonFile);
        String parsedContent = parseWorkspaceJsonContent(jsonContent, args);
        try (JsonReader jsonReader = Json.createReader(new StringReader(parsedContent))) {
          return jsonReader.readObject();
        }
      } catch (URISyntaxException e) {
        throw new TaskRouterException(String.format("Wrong uri to find %s: %s",
          configFileName, e.getMessage()));
      } catch (IOException e) {
        throw new TaskRouterException(String.format("Error while reading %s: %s",
          configFileName, e.getMessage()));
      }
    }).orElseThrow(
      () -> new TaskRouterException("There's no valid configuration in " + configFileName));
  }

  private static String parseWorkspaceJsonContent(final String unparsedContent,
                                                  final String... args) {
    Map<String, String> values = new HashMap<>();
    values.put("host", args[0]);
    values.put("bob_number", args[1]);
    values.put("alice_number", args[2]);
    StrSubstitutor strSubstitutor = new StrSubstitutor(values, "%(", ")s");
    return strSubstitutor.replace(unparsedContent);
  }

  public static String createWorkFlowJsonConfig(WorkspaceFacade workspaceFacade,
                                                JsonObject workflowJson) {
    try {
      JsonArray routingConfigRules = workflowJson.getJsonArray("routingConfiguration");
      TaskQueue defaultQueue = workspaceFacade.findTaskQueueByName("Default")
        .orElseThrow(() -> new TaskRouterException("Default queue not found"));
      WorkflowRuleTarget defaultRuleTarget
        = new WorkflowRuleTarget(defaultQueue.getSid(), "1=1", 1, 30);
      List<WorkflowRule> rules = routingConfigRules.getValuesAs(JsonObject.class).stream()
        .map(ruleJson -> {
          String ruleQueueName = ruleJson.getString("targetTaskQueue");
          TaskQueue ruleQueue = workspaceFacade.findTaskQueueByName(ruleQueueName).orElseThrow(
            () -> new TaskRouterException(String.format("%s queue not found", ruleQueueName)));
          WorkflowRuleTarget queueRuleTarget = new WorkflowRuleTarget(ruleQueue.getSid());
          queueRuleTarget.setPriority(5);
          queueRuleTarget.setTimeout(30);
          List<WorkflowRuleTarget> ruleTargets = Arrays.asList(queueRuleTarget, defaultRuleTarget);
          return new WorkflowRule(ruleJson.getString("expression"), ruleTargets);
        }).collect(Collectors.toList());
      WorkflowConfiguration config = new WorkflowConfiguration(rules, defaultRuleTarget);
      return config.toJSON();
    } catch (Exception ex) {
      throw new TaskRouterException("Error while creating workflow json configuration: "
        + ex.getMessage());
    }
  }
}
