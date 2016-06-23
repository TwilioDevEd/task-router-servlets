package com.twilio.taskrouter.domain.common;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.TwilioTaskRouterClient;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Singleton;
import javax.json.Json;
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
import java.util.stream.Collectors;

/**
 * Twilio settings and helper functions for this app
 */
@Singleton
public class TwilioAppSettings {

  public static final String DIGITS_PARAM = "Digits";

  public static final String EVENT_TYPE_PARAM = "EventType";

  public static final String TASK_ATTRIBUTES_PARAM = "TaskAttributes";

  public static final List<String> DESIRABLE_EVENTS =
    Arrays.asList("workflow.timeout", "task.canceled", "reservation.canceled");

  private final TwilioRestClient twilioRestClient;

  private final TwilioTaskRouterClient twilioTaskRouterClient;

  private String workFlowSID;

  private String postWorkActivitySID;

  private String dequeuInstruction;

  private List<String> activePhoneNumbers;

  public TwilioAppSettings() {
    String twilioAccountSid = Optional.ofNullable(System.getenv("TWILIO_ACCOUNT_SID")).orElseThrow(
      () -> new TaskRouterException("TWILIO_ACCOUNT_SID is not set in the environment"));
    String twilioAuthToken = Optional.ofNullable(System.getenv("TWILIO_AUTH_TOKEN")).orElseThrow(
      () -> new TaskRouterException("TWILIO_AUTH_TOKEN is not set in the environment"));
    twilioRestClient = new TwilioRestClient(twilioAccountSid, twilioAuthToken);
    twilioTaskRouterClient = new TwilioTaskRouterClient(twilioAccountSid, twilioAuthToken);
  }

  public String getWorkFlowSID() {
    if (workFlowSID == null) {
      this.workFlowSID = Optional.ofNullable(System.getenv("WORKFLOW_SID")).orElseThrow(
        () -> new TaskRouterException("WORKFLOW_SID is not set in the environment"));
    }
    return workFlowSID;
  }

  public String getPostWorkActivitySID() {
    if (postWorkActivitySID == null) {
      this.postWorkActivitySID = Optional.ofNullable(System.getenv("POST_WORK_ACTIVITY_SID"))
        .orElseThrow(() ->
          new TaskRouterException("POST_WORK_ACTIVITY_SID is not set in the environment"));
    }
    return postWorkActivitySID;
  }

  public String getDequeuInstruction() {
    if (dequeuInstruction == null) {
      dequeuInstruction = Json.createObjectBuilder()
        .add("instruction", "dequeue")
        .add("post_work_activity_sid", getPostWorkActivitySID())
        .build().toString();
    }
    return dequeuInstruction;
  }

  public List<String> getActivePhoneNumbers() {
    if (activePhoneNumbers == null) {
      activePhoneNumbers = twilioRestClient.getAccount()
        .getIncomingPhoneNumbers().getPageData().stream()
        .map(IncomingPhoneNumber::getPhoneNumber).map(Utils::formatPhoneNumberToUSInternational)
        .collect(Collectors.toList());
    }
    return activePhoneNumbers;
  }

  public TwilioTaskRouterClient getTwilioTaskRouterClient() {
    return twilioTaskRouterClient;
  }

  public void hangUpCall(String callSID) throws TwilioRestException {
    Call call = twilioRestClient.getAccount().getCall(callSID);
    BasicNameValuePair hangUpCmd = new BasicNameValuePair("Status", "completed");
    call.update(Arrays.asList(hangUpCmd));
  }

  public JsonObject createWorkspaceConfig(String[] args) {
    final String configFileName = "workspace.json";
    Optional<URL> url =
      Optional.ofNullable(this.getClass().getResource(File.separator + configFileName));
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

  private String parseWorkspaceJsonContent(final String unparsedContent,
                                           final String... args) {
    Map<String, String> values = new HashMap<>();
    values.put("host", args[0]);
    values.put("bob_number", args[1]);
    values.put("alice_number", args[2]);
    StrSubstitutor strSubstitutor = new StrSubstitutor(values, "%(", ")s");
    return strSubstitutor.replace(unparsedContent);
  }

}
