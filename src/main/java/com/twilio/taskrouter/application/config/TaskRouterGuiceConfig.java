package com.twilio.taskrouter.application.config;

import com.google.inject.AbstractModule;
import com.twilio.taskrouter.domain.model.WorkspaceFacade;

import java.util.logging.Logger;

/**
 * Provide the resources related to task router
 */
public class TaskRouterGuiceConfig extends AbstractModule {

  private static final Logger LOG = Logger.getLogger(TaskRouterGuiceConfig.class.getName());

  @Override
  protected void configure() {
    bind(WorkspaceFacade.class)
      .toProvider(WorkspaceProvider.class);
  }
}
