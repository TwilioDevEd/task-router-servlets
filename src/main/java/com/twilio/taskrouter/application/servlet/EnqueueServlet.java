package com.twilio.taskrouter.application.servlet;

import com.twilio.sdk.verbs.Enqueue;
import com.twilio.sdk.verbs.Task;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Parses a selected product, creating a Task on Task Router Workflow
 */
@Singleton
public class EnqueueServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(EnqueueServlet.class.getName());

  private final TwilioAppSettings twilioSettings;

  @Inject
  public EnqueueServlet(final TwilioAppSettings twilioSettings) {
    this.twilioSettings = twilioSettings;
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
    IOException {
    String selectedProduct = getSelectedProduct(req);
    final TwiMLResponse twimlResponse = new TwiMLResponse();
    final Enqueue enqueue = new Enqueue();
    enqueue.setWorkflowSid(twilioSettings.getWorkFlowSID());
    try {
      enqueue.append(new Task(String.format("{\"selected_product\": \"%s\"}", selectedProduct)));
      twimlResponse.append(enqueue);
    } catch (final TwiMLException e) {
      e.printStackTrace();
    }
    resp.setContentType("application/xml");
    resp.getWriter().print(twimlResponse.toXML());
  }

  public String getSelectedProduct(HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter(TwilioAppSettings.DIGITS_PARAM))
      .filter(x -> x.equals("1")).map((first) -> "ProgrammableSMS").orElse("ProgrammableVoice");
  }
}
