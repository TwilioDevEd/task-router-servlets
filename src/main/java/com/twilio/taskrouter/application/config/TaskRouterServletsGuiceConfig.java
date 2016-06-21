package com.twilio.taskrouter.application.config;

import com.google.inject.persist.PersistFilter;
import com.google.inject.servlet.ServletModule;
import com.twilio.taskrouter.application.servlet.AssignmentServlet;
import com.twilio.taskrouter.application.servlet.EnqueueServlet;
import com.twilio.taskrouter.application.servlet.EventsServlet;
import com.twilio.taskrouter.application.servlet.IncomingCallServlet;
import com.twilio.taskrouter.application.servlet.IndexServlet;

/**
 * Configure the servlets for the project
 */
public class TaskRouterServletsGuiceConfig extends ServletModule {

  @Override
  public void configureServlets() {
    filter("/*").through(PersistFilter.class);
    serve("/").with(IndexServlet.class);
    serve("/call/incoming").with(IncomingCallServlet.class);
    serve("/call/enqueue").with(EnqueueServlet.class);
    serve("/assignment").with(AssignmentServlet.class);
    serve("/events").with(EventsServlet.class);
  }
}
