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

@Singleton
public class IndexServlet extends HttpServlet {

  private final TwilioAppSettings twilioAppSettings;

  private final MissedCallRepository missedCallRepository;

  @Inject
  public IndexServlet(TwilioAppSettings twilioAppSettings,
                      MissedCallRepository missedCallRepository) {
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
