package com.twilio.taskrouter.domain.common;

import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.CallFetcher;
import com.twilio.rest.api.v2010.account.CallUpdater;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import com.twilio.taskrouter.domain.model.PhoneNumber;
import io.github.cdimascio.dotenv.Dotenv;
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

  private String twilioAccountSid;

  private String twilioAuthToken;

  private String workflowSid;

  private String workspaceSid;

  private String postWorkActivitySid;

  private String email;

  private PhoneNumber phoneNumber;

  private static Dotenv dotenv = Dotenv.load();

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
      LOG.info("Welcome to TaskRouter tutorial for servlets. First time running....");
      String phoneNumberStr = Optional.ofNullable(dotenv.get("TWILIO_NUMBER")).orElseThrow(
        () -> new TaskRouterException("TWILIO_NUMBER is not set in the environment"));
      phoneNumber = new PhoneNumber(phoneNumberStr);
      email = Optional.ofNullable(dotenv.get("MISSED_CALLS_EMAIL_ADDRESS")).orElseThrow(
        () -> new TaskRouterException("MISSED_CALLS_EMAIL_ADDRESS is not set in the environment"));
    }
    twilioAccountSid = Optional.ofNullable(dotenv.get("TWILIO_ACCOUNT_SID")).orElseThrow(
      () -> new TaskRouterException("TWILIO_ACCOUNT_SID is not set in the environment"));
    twilioAuthToken = Optional.ofNullable(dotenv.get("TWILIO_AUTH_TOKEN")).orElseThrow(
      () -> new TaskRouterException("TWILIO_AUTH_TOKEN is not set in the environment"));


    Twilio.init(twilioAccountSid, twilioAuthToken);
    twilioRestClient = new TwilioRestClient.Builder(twilioAccountSid, twilioAuthToken).build();
  }

  public TwilioRestClient getTwilioRestClient() {
    return twilioRestClient;
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

  public void redirectToVoiceMail(String callSID, String msgToUser) {
    try {
      String routeUrl = String.format("http://twimlets.com/voicemail?Email=%s&Message=%s",
        getEmail(), URLEncoder.encode(msgToUser, "UTF-8"));
      Call call = new CallFetcher(callSID).fetch(twilioRestClient);
      List<NameValuePair> params = new ArrayList<>();
      params.add(new BasicNameValuePair("Url", routeUrl));
      params.add(new BasicNameValuePair("Method", "POST"));
      new CallUpdater(call.getSid())
        .setUrl(routeUrl)
        .setMethod(HttpMethod.POST)
        .update(twilioRestClient);
    } catch (UnsupportedEncodingException e) {
      throw new TaskRouterException("Error converting message to the user to a valid url "
        + e.getMessage());
    }
  }
}
