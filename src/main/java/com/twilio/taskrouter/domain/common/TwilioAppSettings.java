package com.twilio.taskrouter.domain.common;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.TwilioTaskRouterClient;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import com.twilio.taskrouter.domain.model.PhoneNumber;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Singleton;
import javax.json.Json;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    Arrays.asList("workflow.timeout", "task.canceled");

  private final TwilioRestClient twilioRestClient;

  private final TwilioTaskRouterClient twilioTaskRouterClient;

  private String workFlowSID;

  private String postWorkActivitySID;

  private String email;

  private String dequeuInstruction;

  private List<PhoneNumber> activePhoneNumbers;

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

  public String getEmail() {
    if (email == null) {
      this.email = Optional.ofNullable(System.getenv("MISSED_CALLS_EMAIL_ADDRESS")).orElseThrow(
        () -> new TaskRouterException("MISSED_CALLS_EMAIL_ADDRESS is not set in the environment"));
    }
    return email;
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

  public List<PhoneNumber> getActivePhoneNumbers() {
    if (activePhoneNumbers == null) {
      activePhoneNumbers = Optional.ofNullable(System.getenv("TWILIO_NUMBER"))
        .map(PhoneNumber::new).map(Arrays::asList).orElseGet(() -> {
          return twilioRestClient.getAccount()
            .getIncomingPhoneNumbers().getPageData().stream()
            .map(IncomingPhoneNumber::getPhoneNumber).map(PhoneNumber::new)
            .collect(Collectors.toList());
        });
    }
    return activePhoneNumbers;
  }

  public TwilioTaskRouterClient getTwilioTaskRouterClient() {
    return twilioTaskRouterClient;
  }

  public void leaveMessage(String callSID, String msgToUser) throws TwilioRestException {
    try {
      String routeUrl = String.format("http://twimlets.com/voicemail?Email=%s&Message=%s",
        getEmail(), URLEncoder.encode(msgToUser, "UTF-8"));
      Call call = twilioRestClient.getAccount().getCall(callSID);
      List<NameValuePair> params = new ArrayList<>();
      params.add(new BasicNameValuePair("Url", routeUrl));
      params.add(new BasicNameValuePair("Method", "POST"));
      call.update(params);
    } catch (UnsupportedEncodingException e) {
      throw new TaskRouterException("Error converting message to the user to a valid url "
        + e.getMessage());
    }
  }
}
