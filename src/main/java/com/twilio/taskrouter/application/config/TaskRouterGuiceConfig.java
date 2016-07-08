package com.twilio.taskrouter.application.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.common.Utils;
import com.twilio.taskrouter.domain.model.WorkspaceFacade;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * Provide the resources related to task router
 */
public class TaskRouterGuiceConfig extends AbstractModule {

  private static final Logger LOG = Logger.getLogger(TaskRouterGuiceConfig.class.getName());

  @Override
  protected void configure() {
    try {
      File workspacePropertiesFile = new File(TwilioAppSettings.WORKSPACE_PROPERTIES_FILE_PATH);
      if (!(workspacePropertiesFile.exists() && workspacePropertiesFile.isFile())) {
        LOG.severe("There's no workspace registered. Please execute first "
          + "the createWorkspace task from gradle");
        exit(1);
      }
      Properties workspaceProperties = Utils.loadProperties(workspacePropertiesFile);
      Names.bindProperties(binder(), workspaceProperties);
    } catch (IOException e) {
      LOG.severe("Could not read workspace.properties");
    }

    bind(WorkspaceFacade.class)
      .toProvider(WorkspaceProvider.class);
  }
}
