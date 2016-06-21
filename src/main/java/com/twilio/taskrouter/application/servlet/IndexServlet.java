package com.twilio.taskrouter.application.servlet;

import com.google.inject.persist.Transactional;
import com.twilio.taskrouter.domain.model.MissedCall;
import com.twilio.taskrouter.domain.repository.MissedCallRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class IndexServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(IndexServlet.class.getName());

  private final MissedCallRepository missedCallRepository;

  @Inject
  public IndexServlet(final MissedCallRepository missedCallRepository) {
    this.missedCallRepository = missedCallRepository;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
    IOException {
    req.setAttribute("missed_calls", missedCallRepository.getAll());
    req.getRequestDispatcher("index.jsp").forward(req, resp);
  }

  @Override
  @Transactional
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      LOG.info("Installing sample missed calls...");
      MissedCall referenceMissedCall = new MissedCall("+14157234000", "ProgrammableSMS");
      missedCallRepository.add(referenceMissedCall);
    } catch (Exception ex) {
      LOG.log(Level.SEVERE, "Unexpected error during setup: " + ex.getMessage());
    }
  }

}
