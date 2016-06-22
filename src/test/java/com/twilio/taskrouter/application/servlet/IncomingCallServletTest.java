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

import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class IncomingCallServletTest {

  @Mock
  private HttpServletRequest requestMock;

  @Mock
  private HttpServletResponse responseMock;

  @Mock
  private TwilioAppSettings twilioAppSettingsMock;

  @InjectMocks
  private IncomingCallServlet incomingCallServlet;

  @Test
  public void shouldReturnXmlContent() throws Exception {
    when(responseMock.getWriter()).thenReturn(mock(PrintWriter.class));
    incomingCallServlet.doPost(requestMock, responseMock);
    verify(responseMock, times(1)).setContentType("application/xml");
  }

  @Test
  public void shouldReturnMessageIncontent() throws Exception {
    when(responseMock.getWriter()).thenReturn(mock(PrintWriter.class));
    incomingCallServlet.doPost(requestMock, responseMock);
    verify(responseMock.getWriter(), times(1)).print(contains(
      "For Programmable SMS, press one. For Voice, press any other key."));
  }
}
