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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Selects a product by creating a Task on the Task Router Workflow
 */
@Singleton
public class EnqueueServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(EnqueueServlet.class.getName());

  private final String workflowSid;

  @Inject
  public EnqueueServlet(TwilioAppSettings twilioSettings) {
    this.workflowSid = twilioSettings.getWorkflowSid();
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
    IOException {
    String selectedProduct = getSelectedProduct(req);
    final TwiMLResponse twimlResponse = new TwiMLResponse();
    final Enqueue enqueue = new Enqueue();
    enqueue.setWorkflowSid(workflowSid);
    try {
      enqueue.append(new Task(String.format("{\"selected_product\": \"%s\"}", selectedProduct)));
      twimlResponse.append(enqueue);
    } catch (final TwiMLException e) {
      LOG.log(Level.SEVERE, "Error while appending enqueue task to the response", e);
    }
    resp.setContentType("application/xml");
    resp.getWriter().print(twimlResponse.toXML());
  }

  public String getSelectedProduct(HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter("Digits"))
      .filter(x -> x.equals("1")).map((first) -> "ProgrammableSMS").orElse("ProgrammableVoice");
  }
}
