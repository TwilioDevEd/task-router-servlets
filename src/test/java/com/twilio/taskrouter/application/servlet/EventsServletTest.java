package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import com.twilio.taskrouter.domain.model.MissedCall;
import com.twilio.taskrouter.domain.repository.MissedCallRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.twilio.taskrouter.application.servlet.EventsServlet.LEAVE_MSG;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventsServletTest {

  @Mock
  private HttpServletRequest requestMock;

  @Mock
  private HttpServletResponse responseMock;

  @Mock
  private TwilioAppSettings twilioAppSettingsMock;

  @Mock
  private MissedCallRepository missedCallRepository;

  @InjectMocks
  private EventsServlet eventsServlet;

  @Test
  public void shouldNotReturnContent() throws Exception {
    eventsServlet.doPost(requestMock, responseMock);
    verify(responseMock, never()).getWriter();
  }

  @Test
  public void testDoPost() throws Exception {
    when(requestMock.getParameter(anyString())).thenReturn("task.canceled");
    JsonObject taskAttribs = Json.createObjectBuilder()
      .add("from", "from-content")
      .add("selected_product", "selected_product-content")
      .add("call_sid", "call_sid-content")
      .build();
    when(requestMock.getParameter(TwilioAppSettings.TASK_ATTRIBUTES_PARAM))
      .thenReturn(taskAttribs.toString());

    eventsServlet.doPost(requestMock, responseMock);

    verify(twilioAppSettingsMock, times(1)).leaveMessage("call_sid-content", LEAVE_MSG);
    verify(missedCallRepository, times(1)).add(any(MissedCall.class));
  }
}
