package com.twilio.taskrouter.domain.common;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.twilio.taskrouter.domain.error.TaskRouterException;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Complementary functions
 */
public final class Utils {

  private Utils() {
  }

  /**
   * Returns the text content of a {@link File}
   *
   * @param file {@link File} to read
   * @return {@link String} not <code>null</code>
   */
  public static String readFileContent(File file) throws IOException {
    StringBuffer result = new StringBuffer();
    Files.lines(file.toPath()).forEach(result::append);
    return result.toString();
  }

  /**
   * Copy some texto to the system clipboard
   *
   * @param text {@link String} to copy
   */
  public static void copyTextToClipboad(String text) {
    StringSelection selection = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, selection);
  }

  /**
   * Converts a phone number into the American International Standard
   *
   * @param phoneNumber Phone number
   * @return {@link String} phone number, not <code>null</code>
   */
  public static String formatPhoneNumberToUSInternational(String phoneNumber) {
    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    try {
      Phonenumber.PhoneNumber usPhoneNumber = phoneUtil.parse(phoneNumber, "US");
      return phoneUtil.format(usPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    } catch (NumberParseException e) {
      throw new TaskRouterException("Invalid phone format: " + e.toString());
    }
  }

}
