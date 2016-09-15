package com.twilio.taskrouter.application.servlet;

import com.twilio.rest.taskrouter.v1.workspace.Activity;
import com.twilio.rest.taskrouter.v1.workspace.Worker;
import com.twilio.taskrouter.domain.model.WorkspaceFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
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
  private WorkspaceFacade workspaceFacadeMock;

  private MessageServlet messageServlet;

  @Before
  public void setUp() throws Exception {
    this.messageServlet = new MessageServlet(workspaceFacadeMock);
    when(responseMock.getWriter()).thenReturn(mock(PrintWriter.class));

    Worker workerMock = mock(Worker.class);
    Map<String, Worker> phoneToWorkerMock = new HashMap<>();
    phoneToWorkerMock.put(WORKER_PHONE_MOCK, workerMock);

    when(workspaceFacadeMock.getPhoneToWorker())
      .thenReturn(phoneToWorkerMock);

    Constructor<Activity> idleConstructor = Activity.class.getDeclaredConstructor(
      String.class, Boolean.class, String.class, String.class, String.class, String.class, String.class
    );
    idleConstructor.setAccessible(true);
    Activity idleActivity = idleConstructor.newInstance(
      "WACIDLEXXXX", true, "2010-01-01", "2010-01-01", "idle", "WACIDLEXXXX", "WACIDLEXXXX"
    );

    when(workspaceFacadeMock.findActivityByName("Idle"))
      .thenReturn(Optional.of(idleActivity));

    Constructor<Activity> offlineActivityConstructor = Activity.class.getDeclaredConstructor(
      String.class, Boolean.class, String.class, String.class, String.class, String.class, String.class
    );
    offlineActivityConstructor.setAccessible(true);
    Activity offlineActivity = offlineActivityConstructor.newInstance(
      "WACOFFLINEXXXX", true, "2010-01-01", "2010-01-01", "off", "WACOFFLINEXXXX", "WACOFFLINEXXXX"
    );

    when(workspaceFacadeMock.findActivityByName("Offline"))
      .thenReturn(Optional.of(offlineActivity));

    when(workspaceFacadeMock.findWorkerByPhone(anyString())).thenCallRealMethod();
  }

  @Test
  public void shouldUpdateWorkerStatus() throws Exception {
    when(requestMock.getParameter("Body")).thenReturn("on");
    when(requestMock.getParameter("From")).thenReturn(WORKER_PHONE_MOCK);

    messageServlet.doPost(requestMock, responseMock);

    verify(workspaceFacadeMock, times(1)).updateWorkerStatus(any(Worker.class), anyString());
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
