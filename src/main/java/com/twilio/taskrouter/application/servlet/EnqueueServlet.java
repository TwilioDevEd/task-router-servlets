package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Enqueue;
import com.twilio.twiml.voice.Task;

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

import static java.lang.String.format;

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
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    String selectedProduct = getSelectedProduct(req);
    Task task = new Task
      .Builder(format("{\"selected_product\": \"%s\"}", selectedProduct))
      .build();

    Enqueue enqueue = new Enqueue.Builder().task(task).workflowSid(workflowSid).build();

    VoiceResponse voiceResponse = new VoiceResponse.Builder().enqueue(enqueue).build();
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
