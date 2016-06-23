package com.twilio.taskrouter.application.servlet;

import com.google.inject.persist.Transactional;
import com.twilio.sdk.TwilioRestException;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.model.MissedCall;
import com.twilio.taskrouter.domain.repository.MissedCallRepository;

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
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Servlet for Events callback for missed calls
 */
@Singleton
public class EventsServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(EventsServlet.class.getName());

  private final TwilioAppSettings twilioSettings;

  private final MissedCallRepository missedCallRepository;

  @Inject
  public EventsServlet(final TwilioAppSettings twilioSettings,
                       final MissedCallRepository missedCallRepository) {
    this.twilioSettings = twilioSettings;
    this.missedCallRepository = missedCallRepository;
  }

  @Override
  @Transactional
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
    IOException {
    Optional.ofNullable(req.getParameter(TwilioAppSettings.EVENT_TYPE_PARAM))
      .filter(TwilioAppSettings.DESIRABLE_EVENTS::contains)
      .flatMap(eventType -> getTaskAttributes(req)).ifPresent(this::addMissingCallAndHangUp);
  }

  public void addMissingCallAndHangUp(JsonObject taskAttributesJson) {
    String phoneNumber = taskAttributesJson.getString("from");
    String selectedProduct = taskAttributesJson.getString("selected_product");
    MissedCall missedCall = new MissedCall(phoneNumber, selectedProduct);
    missedCallRepository.add(missedCall);
    LOG.info("Added Missing Call: " + missedCall);
    String callSid = taskAttributesJson.getString("call_sid");
    try {
      twilioSettings.hangUpCall(callSid);
    } catch (TwilioRestException e) {
      LOG.warning(String.format("Error while hanging the call '%s': %s",
        callSid, e.getMessage()));
    }
  }

  public Optional<JsonObject> getTaskAttributes(HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter(TwilioAppSettings.TASK_ATTRIBUTES_PARAM))
      .map(jsonRequest -> Json.createReader(new StringReader(jsonRequest)).readObject());
  }
}
