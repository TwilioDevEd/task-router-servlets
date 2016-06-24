package com.twilio.taskrouter.domain.common;

import com.twilio.taskrouter.domain.error.TaskRouterException;
import org.junit.Assert;
import org.junit.Test;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;


public class UtilsTest {

  private static final Logger LOG = Logger.getLogger(UtilsTest.class.getName());

  private static URI getResourceURI(String filePath) {
    Optional<URL> url =
      Optional.ofNullable(UtilsTest.class.getResource(File.separator + filePath));
    return url.map(u -> {
      try {
        return u.toURI();
      } catch (Exception e) {
        throw new TaskRouterException(e);
      }
    }).orElseThrow(
      () -> new TaskRouterException(String.format("Not possible to retrieve resource: %s",
        filePath)));
  }

  @Test
  public void readFileContent() throws Exception {
    URI resourceURI = getResourceURI("sample.txt");
    String s = Utils.readFileContent(new File(resourceURI));
    Assert.assertNotNull(s);
    Assert.assertEquals("The content was read correctly", s);
  }

  @Test
  public void copyTextToClipboad() throws Exception {
    try {
      String content = "Content\nwith new line";
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Utils.copyTextToClipboad(content);
      String result = (String) clipboard.getData(DataFlavor.stringFlavor);
      Assert.assertEquals(content, result);
    } catch (Throwable ex) {
      LOG.warning("The clipboard features is not available in this system");
    }
  }

  @Test
  public void formatPhoneNumberToUSInternational() throws Exception {
    Assert.assertEquals("The international format for the numbers is not the expected",
      "+1 415-723-4000",
      Utils.formatPhoneNumberToUSInternational("+14157234000")
    );
  }

}
