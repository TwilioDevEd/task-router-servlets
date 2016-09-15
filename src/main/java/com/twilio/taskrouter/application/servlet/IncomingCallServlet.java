package com.twilio.taskrouter.application.servlet;


import com.twilio.twiml.Gather;
import com.twilio.twiml.Method;
import com.twilio.twiml.Say;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;

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
    try {
      final VoiceResponse twimlResponse = new VoiceResponse.Builder()
        .gather(new Gather.Builder()
          .action("/call/enqueue")
          .numDigits(1)
          .timeout(10)
          .method(Method.POST)
          .say(new Say
            .Builder("For Programmable SMS, press one. For Voice, press any other key.")
            .build()
          )
          .build()
        ).build();

      resp.setContentType("application/xml");
      resp.getWriter().print(twimlResponse.toXml());
    } catch (TwiMLException e) {
      LOG.log(Level.SEVERE, "Unexpected error while creating incoming call response", e);
      throw new RuntimeException(e);
    }
  }

}
