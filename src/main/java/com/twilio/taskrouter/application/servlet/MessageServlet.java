package com.twilio.taskrouter.application.servlet;

import com.google.inject.Singleton;
import com.twilio.taskrouter.domain.model.WorkspaceFacade;
import com.twilio.twiml.Sms;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the messages sent by workers for activate/deactivate
 * themselves for receiving calls from users
 */
@Singleton
public class MessageServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(MessageServlet.class.getName());

  private final WorkspaceFacade workspace;

  @Inject
  public MessageServlet(WorkspaceFacade workspace) {
    this.workspace = workspace;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    final VoiceResponse twimlResponse;
    final String newStatus = getNewWorkerStatus(req);
    final String workerPhone = req.getParameter("From");

    try {
      Sms responseSms = workspace.findWorkerByPhone(workerPhone).map(worker -> {
        workspace.updateWorkerStatus(worker, newStatus);
        return new Sms.Builder(String.format("Your status has changed to %s", newStatus)).build();
      }).orElseGet(() -> new Sms.Builder("You are not a valid worker").build());

      twimlResponse = new VoiceResponse.Builder().sms(responseSms).build();
      resp.setContentType("application/xml");
      resp.getWriter().print(twimlResponse.toXml());

    } catch (TwiMLException e) {
      LOG.log(Level.SEVERE, "Error while providing answer to a workers' sms", e);
    }

  }

  private String getNewWorkerStatus(HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter("Body"))
      .filter(x -> x.equals("off")).map((first) -> "Offline").orElse("Idle");
  }

}
