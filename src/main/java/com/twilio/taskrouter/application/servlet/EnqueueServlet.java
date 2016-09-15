package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.twiml.EnqueueTask;
import com.twilio.twiml.Task;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;

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
 * Selects a product by creating a Task on the TaskRouter Workflow
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
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String selectedProduct = getSelectedProduct(req);
    Task task = new Task.Builder().data(String.format("{\"selected_product\": \"%s\"}", selectedProduct)).build();

    EnqueueTask enqueueTask = new EnqueueTask.Builder(task).workflowSid(workflowSid).build();

    VoiceResponse voiceResponse = new VoiceResponse.Builder().enqueue(enqueueTask).build();
    resp.setContentType("application/xml");
    try {
      resp.getWriter().print(voiceResponse.toXml());
    } catch (TwiMLException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private String getSelectedProduct(HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter("Digits"))
      .filter(x -> x.equals("1")).map((first) -> "ProgrammableSMS").orElse("ProgrammableVoice");
  }
}
