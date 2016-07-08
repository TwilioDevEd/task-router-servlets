package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssignmentServletTest {

  @Mock
  private HttpServletRequest requestMock;

  @Mock
  private HttpServletResponse responseMock;

  @Mock
  private TwilioAppSettings twilioAppSettingsMock;

  @InjectMocks
  private AssignmentServlet assignmentServlet;

  @Test
  public void shouldRequestTwilioAppSettingsDequeueInstruction() throws Exception {
    when(responseMock.getWriter()).thenReturn(mock(PrintWriter.class));
    assignmentServlet.doPost(requestMock, responseMock);
    verify(twilioAppSettingsMock, times(1)).getDeQueueInstruction();
  }

  @Test
  public void shouldReturnAJson() throws Exception {
    when(responseMock.getWriter()).thenReturn(mock(PrintWriter.class));
    when(twilioAppSettingsMock.getDeQueueInstruction()).thenReturn("{}");

    assignmentServlet.doPost(requestMock, responseMock);

    verify(responseMock, times(1)).setContentType("application/json");
    verify(responseMock.getWriter(), times(1)).print("{}");
  }

}
