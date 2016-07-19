package com.twilio.taskrouter.application.servlet;

import com.twilio.sdk.resource.instance.taskrouter.Activity;
import com.twilio.sdk.resource.instance.taskrouter.Worker;
import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.model.WorkspaceFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageServletTest {

  private static final String WORKER_PHONE_MOCK = "+12345678990";

  @Mock
  private HttpServletRequest requestMock;

  @Mock
  private HttpServletResponse responseMock;

  @Mock
  private TwilioAppSettings twilioAppSettingsMock;

  @Mock
  private WorkspaceFacade workspaceFacadeMock;

  @InjectMocks
  private MessageServlet messageServlet;

  @Before
  public void setUp() throws Exception {
    when(responseMock.getWriter()).thenReturn(mock(PrintWriter.class));

    Worker workerMock = mock(Worker.class);
    Map<String, Worker> phoneToWorkerMock = new HashMap<>();
    phoneToWorkerMock.put(WORKER_PHONE_MOCK, workerMock);

    when(workspaceFacadeMock.getPhoneToWorker())
      .thenReturn(phoneToWorkerMock);

    Activity idleActivity = mock(Activity.class);
    when(idleActivity.getSid()).thenReturn("WACIDLEXXXX");
    when(workspaceFacadeMock.findActivityByName("Idle"))
      .thenReturn(Optional.of(idleActivity));

    Activity offlineActivity = mock(Activity.class);
    when(offlineActivity.getSid()).thenReturn("WACOFFLINEXXXX");
    when(workspaceFacadeMock.findActivityByName("Offline"))
      .thenReturn(Optional.of(offlineActivity));

    when(workspaceFacadeMock.findWorkerByPhone(anyString())).thenCallRealMethod();
  }

  @Test
  public void shouldChangeToIdleStatus() throws Exception {
    when(requestMock.getParameter("Body")).thenReturn("on");
    when(requestMock.getParameter("From")).thenReturn(WORKER_PHONE_MOCK);

    messageServlet.doPost(requestMock, responseMock);

    verify(workspaceFacadeMock, times(1)).findActivityByName("Idle");

    verify(responseMock, times(1)).setContentType("application/xml");
    verify(responseMock.getWriter(), times(1))
      .print("<Response><Sms>Your status has changed to Idle</Sms></Response>");
  }

  @Test
  public void shouldChangeToOfflineStatus() throws Exception {
    when(requestMock.getParameter("Body")).thenReturn("off");
    when(requestMock.getParameter("From")).thenReturn(WORKER_PHONE_MOCK);

    messageServlet.doPost(requestMock, responseMock);

    verify(workspaceFacadeMock, times(1)).findActivityByName("Offline");

    verify(responseMock, times(1)).setContentType("application/xml");
    verify(responseMock.getWriter(), times(1))
      .print("<Response><Sms>Your status has changed to Offline</Sms></Response>");
  }

  @Test
  public void testNoValidWorker() throws Exception {
    when(requestMock.getParameter("Body")).thenReturn("on");
    when(requestMock.getParameter("From")).thenReturn("+122222222222");

    messageServlet.doPost(requestMock, responseMock);

    verify(responseMock, times(1)).setContentType("application/xml");
    verify(responseMock.getWriter(), times(1))
      .print("<Response><Sms>You are not a valid worker</Sms></Response>");
  }

}
