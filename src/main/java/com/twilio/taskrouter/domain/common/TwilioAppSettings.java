package com.twilio.taskrouter.domain.common;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.TwilioTaskRouterClient;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import com.twilio.taskrouter.domain.model.PhoneNumber;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Twilio settings and helper functions for this app
 */
@Singleton
public class TwilioAppSettings {

  public static final String WORKSPACE_PROPERTIES_FILE_PATH = "workspace.properties";

  private static final Logger LOG = Logger.getLogger(TwilioAppSettings.class.getName());

  private final TwilioRestClient twilioRestClient;

  private final TwilioTaskRouterClient twilioTaskRouterClient;

  private String twilioAccountSid;

  private String twilioAuthToken;

  private String workflowSid;

  private String workspaceSid;

  private String postWorkActivitySid;

  private String email;

  private PhoneNumber phoneNumber;

  public TwilioAppSettings() {
    try {
      Properties properties = Utils.loadProperties(new File(WORKSPACE_PROPERTIES_FILE_PATH));
      twilioAccountSid = properties.getProperty("account.sid");
      twilioAuthToken = properties.getProperty("auth.token");
      workflowSid = properties.getProperty("workflow.sid");
      workspaceSid = properties.getProperty("workspace.sid");
      postWorkActivitySid = properties.getProperty("postWorkActivity.sid");
      email = properties.getProperty("email");
      String phoneNumberStr = properties.getProperty("phoneNumber");
      phoneNumber = new PhoneNumber(phoneNumberStr);
    } catch (IOException e) {
      LOG.info("Welcome to Task Router tutorial for servlets. First time running....");
      twilioAccountSid = Optional.ofNullable(System.getenv("TWILIO_ACCOUNT_SID")).orElseThrow(
        () -> new TaskRouterException("TWILIO_ACCOUNT_SID is not set in the environment"));
      twilioAuthToken = Optional.ofNullable(System.getenv("TWILIO_AUTH_TOKEN")).orElseThrow(
        () -> new TaskRouterException("TWILIO_AUTH_TOKEN is not set in the environment"));
      String phoneNumberStr = Optional.ofNullable(System.getenv("TWILIO_NUMBER")).orElseThrow(
        () -> new TaskRouterException("TWILIO_NUMBER is not set in the environment"));
      phoneNumber = new PhoneNumber(phoneNumberStr);
      email = Optional.ofNullable(System.getenv("MISSED_CALLS_EMAIL_ADDRESS")).orElseThrow(
        () -> new TaskRouterException("MISSED_CALLS_EMAIL_ADDRESS is not set in the environment"));
    }
    twilioRestClient = new TwilioRestClient(twilioAccountSid, twilioAuthToken);
    twilioTaskRouterClient = new TwilioTaskRouterClient(twilioAccountSid, twilioAuthToken);
  }

  public TwilioRestClient getTwilioRestClient() {
    return twilioRestClient;
  }

  public TwilioTaskRouterClient getTwilioTaskRouterClient() {
    return twilioTaskRouterClient;
  }

  public String getTwilioAccountSid() {
    return twilioAccountSid;
  }

  public String getTwilioAuthToken() {
    return twilioAuthToken;
  }

  public String getWorkflowSid() {
    return workflowSid;
  }

  public String getPostWorkActivitySid() {
    return postWorkActivitySid;
  }

  public String getWorkspaceSid() {
    return workspaceSid;
  }

  public String getEmail() {
    return email;
  }

  public PhoneNumber getPhoneNumber() {
    return phoneNumber;
  }

  public void redirectToVoiceMail(String callSID, String msgToUser) throws TwilioRestException {
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
