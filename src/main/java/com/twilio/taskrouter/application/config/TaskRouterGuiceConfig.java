package com.twilio.taskrouter.application.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Provide the Workspace and related resources
 */
public class TaskRouterGuiceConfig extends AbstractModule {

  private static final Logger LOG = Logger.getLogger(TaskRouterGuiceConfig.class.getName());

  @Override
  protected void configure() {
    Properties workspaceProperties = new Properties();
    try (InputStream in = getClass().getResourceAsStream("workspace.properties")) {
      workspaceProperties.load(in);
    } catch (IOException e) {
      LOG.severe("Could not read workspace.properties");
    }
    Names.bindProperties(binder(), workspaceProperties);
  }
}
