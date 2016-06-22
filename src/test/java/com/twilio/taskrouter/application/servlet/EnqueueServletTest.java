package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnqueueServletTest {

  @Mock
  private HttpServletRequest requestMock;

  @Mock
  private HttpServletResponse responseMock;

  @Mock
  private TwilioAppSettings twilioAppSettingsMock;

  @InjectMocks
  private EnqueueServlet enqueueServlet;

  @Test
  public void shouldReturnATwimlResponse() throws Exception {
    when(responseMock.getWriter()).thenReturn(mock(PrintWriter.class));
    enqueueServlet.doPost(requestMock, responseMock);
    verify(responseMock, times(1)).setContentType("application/xml");
  }

  @Test
  public void selectedProductShouldBeProgrammableSMS() throws Exception {
    when(requestMock.getParameter(anyString())).thenReturn("1");
    Assert.assertEquals("For 1 the answer was not ProgrammableSMS",
      enqueueServlet.getSelectedProduct(requestMock),
      "ProgrammableSMS");
  }

  @Test
  public void selectedProductShouldBeProgrammableVoice() throws Exception {
    when(requestMock.getParameter(anyString())).thenReturn("2");
    Assert.assertEquals("For 1 the answer was not ProgrammableVoice",
      enqueueServlet.getSelectedProduct(requestMock),
      "ProgrammableVoice");
  }
}
