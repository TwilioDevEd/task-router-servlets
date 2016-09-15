package com.twilio.taskrouter.application.config;

import com.google.inject.Provider;
import com.twilio.http.TwilioRestClient;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.error.TaskRouterException;
import com.twilio.taskrouter.domain.model.WorkspaceFacade;

import javax.inject.Inject;

/**
 * Provides beans for easy working with Twilio
 */
public class WorkspaceProvider implements Provider<WorkspaceFacade> {

  private final TwilioRestClient twilioRestClient;

  private final String workspaceSid;

  @Inject
  public WorkspaceProvider(TwilioAppSettings twilioAppSettings) {
    this.twilioRestClient = twilioAppSettings.getTwilioRestClient();
    this.workspaceSid = twilioAppSettings.getWorkspaceSid();
  }

  @Override
  public WorkspaceFacade get() {
    return WorkspaceFacade.findBySid(workspaceSid, twilioRestClient).orElseThrow(() ->
      new TaskRouterException(String.format("There's no workspace with sid \"%s\"", workspaceSid))
    );
  }
}
