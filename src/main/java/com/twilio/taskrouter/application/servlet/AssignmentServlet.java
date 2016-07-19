package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for Task assignments
 */
@Singleton
public class AssignmentServlet extends HttpServlet {

  private final String dequeueInstruction;

  @Inject
  public AssignmentServlet(TwilioAppSettings twilioAppSettings) {
    dequeueInstruction = Json.createObjectBuilder()
      .add("instruction", "dequeue")
      .add("post_work_activity_sid", twilioAppSettings.getPostWorkActivitySid())
      .build().toString();
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.getWriter().print(dequeueInstruction);
  }
}
