package com.twilio.taskrouter.application.servlet;

import com.google.inject.persist.Transactional;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import com.twilio.taskrouter.domain.model.MissedCall;
import com.twilio.taskrouter.domain.repository.MissedCallRepository;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Servlet for Events callback for missed calls
 */
@Singleton
public class EventsServlet extends HttpServlet {

  private static final String LEAVE_MSG = "Sorry, All agents are busy. Please leave a message. "
    + "We will call you as soon as possible";

  private static final String OFFLINE_MSG = "Your status has changed to Offline. "
    + "Reply with \"On\" to get back Online";

  private static final Logger LOG = Logger.getLogger(EventsServlet.class.getName());

  private final TwilioAppSettings twilioSettings;

  private final MissedCallRepository missedCallRepository;

  @Inject
  public EventsServlet(TwilioAppSettings twilioSettings,
                       MissedCallRepository missedCallRepository) {
    this.twilioSettings = twilioSettings;
    this.missedCallRepository = missedCallRepository;
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
    IOException {
    Optional.ofNullable(req.getParameter("EventType"))
      .ifPresent(eventName -> {
        switch (eventName) {
          case "workflow.timeout":
          case "task.canceled":
            parseAttributes("TaskAttributes", req)
              .ifPresent(this::addMissingCallAndLeaveMessage);
            break;
          case "worker.activity.update":
            Optional.ofNullable(req.getParameter("WorkerActivityName"))
              .filter("Offline"::equals)
              .ifPresent(offlineEvent -> {
                parseAttributes("WorkerAttributes", req)
                  .ifPresent(this::notifyOfflineStatusToWorker);
              });
            break;
          default:
        }
      });
  }

  private Optional<JsonObject> parseAttributes(String parameter, HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter(parameter))
      .map(jsonRequest -> Json.createReader(new StringReader(jsonRequest)).readObject());
  }

  @Transactional
  private void addMissingCallAndLeaveMessage(JsonObject taskAttributesJson) {
    String phoneNumber = taskAttributesJson.getString("from");
    String selectedProduct = taskAttributesJson.getString("selected_product");

    MissedCall missedCall = new MissedCall(phoneNumber, selectedProduct);
    missedCallRepository.add(missedCall);
    LOG.info("Added Missing Call: " + missedCall);

    String callSid = taskAttributesJson.getString("call_sid");
    try {
      twilioSettings.redirectToVoiceMail(callSid, LEAVE_MSG);
    } catch (TwilioRestException e) {
      LOG.warning(String.format("Error leaving message for the call '%s': %s",
        callSid, e.getMessage()));
    }
  }

  private void notifyOfflineStatusToWorker(JsonObject workerAttributesJson) {
    Account account = twilioSettings.getTwilioRestClient().getAccount();
    String workerPhone = workerAttributesJson.getString("contact_uri");

    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("To", workerPhone));
    params.add(new BasicNameValuePair("From", twilioSettings.getPhoneNumber().toString()));
    params.add(new BasicNameValuePair("Body", OFFLINE_MSG));

    try {
      account.getMessageFactory().create(params);
    } catch (TwilioRestException e) {
      throw new TaskRouterException(
        "Error while sending offline status notification to " + workerPhone,
        e);
    }
  }

}
