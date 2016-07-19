package com.twilio.taskrouter.application.servlet;

import com.google.inject.Singleton;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.taskrouter.Activity;
import com.twilio.sdk.resource.instance.taskrouter.Worker;
import com.twilio.sdk.verbs.Sms;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import com.twilio.taskrouter.domain.model.WorkspaceFacade;

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
    final String newStatus = getNewWorkerStatus(req);
    String workerPhone = req.getParameter("From");
    final TwiMLResponse twimlResponse = new TwiMLResponse();

    try {
      Sms responseSms = workspace.findWorkerByPhone(workerPhone).map(worker -> {
        updateWorkerStatus(worker, newStatus);

        return new Sms(String.format("Your status has changed to %s", newStatus));
      }).orElseGet(() -> new Sms("You are not a valid worker"));

      twimlResponse.append(responseSms);
    } catch (TwiMLException e) {
      LOG.log(Level.SEVERE, "Error while providing answer to a workers' sms", e);
    }

    resp.setContentType("application/xml");
    resp.getWriter().print(twimlResponse.toXML());
  }

  public String getNewWorkerStatus(HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter("Body"))
      .filter(x -> x.equals("off")).map((first) -> "Offline").orElse("Idle");
  }

  public void updateWorkerStatus(Worker worker, String activityFriendlyName) {
    Activity activity = workspace.findActivityByName(activityFriendlyName).orElseThrow(() ->
      new TaskRouterException(
        String.format("The activity '%s' doesn't exist in the workspace", activityFriendlyName)
      )
    );

    try {
      worker.updateActivity(activity.getSid());
    } catch (TwilioRestException e) {
      throw new TaskRouterException(String.format(
        "Error while changing %s to %s", worker.getFriendlyName(), activityFriendlyName
      ));
    }
  }

}
