package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.repository.MissedCallRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@Singleton
public class IndexServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(IndexServlet.class.getName());

  private final TwilioAppSettings twilioAppSettings;

  private final MissedCallRepository missedCallRepository;

  @Inject
  public IndexServlet(final TwilioAppSettings twilioAppSettings,
                      final MissedCallRepository missedCallRepository) {
    this.twilioAppSettings = twilioAppSettings;
    this.missedCallRepository = missedCallRepository;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
    IOException {
    req.setAttribute("missedCalls", missedCallRepository.getAll());
    req.setAttribute("settings", twilioAppSettings);
    req.getRequestDispatcher("index.jsp").forward(req, resp);
  }
}
