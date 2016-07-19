package com.twilio.taskrouter.application.servlet;

import com.twilio.sdk.verbs.Gather;
import com.twilio.sdk.verbs.Say;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Returns TwiML instructions to TwilioAppSettings's POST requests
 */
@Singleton
public class IncomingCallServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(IncomingCallServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
    IOException {
    final TwiMLResponse twimlResponse = new TwiMLResponse();
    final Gather gather = new Gather();
    gather.setAction("/call/enqueue");
    gather.setNumDigits(1);
    gather.setTimeout(10);
    gather.setMethod("POST");
    try {
      gather.append(new Say("For Programmable SMS, press one. For Voice, press any other key."));
      twimlResponse.append(gather);
    } catch (TwiMLException e) {
      LOG.log(Level.SEVERE, "Unexpected error while creating incoming call response", e);
    }
    resp.setContentType("application/xml");
    resp.getWriter().print(twimlResponse.toXML());
  }

}
