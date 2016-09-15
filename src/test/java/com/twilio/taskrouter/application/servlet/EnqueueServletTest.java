package com.twilio.taskrouter.application.servlet;

import com.twilio.taskrouter.domain.common.TwilioAppSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.hamcrest.Matchers.hasXPath;
import static org.junit.Assert.assertThat;
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

    private EnqueueServlet enqueueServlet;

    @Before
    public void setUp() {
      when(twilioAppSettingsMock.getWorkflowSid()).thenReturn("WWfXXXXXXXXXXXX");
      this.enqueueServlet = new EnqueueServlet(twilioAppSettingsMock);
    }

    @Test
    public void shouldReturnATwimlResponse() throws Exception {
        when(responseMock.getWriter()).thenReturn(mock(PrintWriter.class));
        enqueueServlet.doPost(requestMock, responseMock);

        verify(responseMock, times(1)).setContentType("application/xml");
    }

    @Test
    public void selectedProductShouldBeProgrammableSMS() throws Exception {
        when(requestMock.getParameter(anyString())).thenReturn("1");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(output);
        when(responseMock.getWriter()).thenReturn(printWriter);

        enqueueServlet.doPost(requestMock, responseMock);

        printWriter.flush();
        String content = new String(output.toByteArray(), "UTF-8");
        Document document = XMLTestHelper.createDocumentFromXml(content);
        Node response = document.getElementsByTagName("Response").item(0);

        assertThat(response, hasXPath(
            "/Response/Enqueue/Task[text() = '{\"selected_product\": \"ProgrammableSMS\"}']"));
    }

    @Test
    public void selectedProductShouldBeProgrammableVoice() throws Exception {
        when(requestMock.getParameter(anyString())).thenReturn("2");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(output);
        when(responseMock.getWriter()).thenReturn(printWriter);

        enqueueServlet.doPost(requestMock, responseMock);

        printWriter.flush();
        String content = new String(output.toByteArray(), "UTF-8");

        Document document = XMLTestHelper.createDocumentFromXml(content);
        Node response = document.getElementsByTagName("Response").item(0);

        assertThat(response, hasXPath(
            "/Response/Enqueue/Task[text() = '{\"selected_product\": \"ProgrammableVoice\"}']"));
    }
}
