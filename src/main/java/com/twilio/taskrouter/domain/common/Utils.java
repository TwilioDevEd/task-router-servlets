package com.twilio.taskrouter.domain.common;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.twilio.taskrouter.domain.error.TaskRouterException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;

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

  public static Properties loadProperties(File propertiesFile) throws IOException {
    final Properties properties;
    try (InputStream in = new FileInputStream(propertiesFile)) {
      properties = new Properties();
      properties.load(in);
    }
    return properties;
  }

  public static void saveProperties(Properties properties, File propertiesFile, String comments)
    throws IOException {
    if (!(propertiesFile.exists() && propertiesFile.isFile())) {
      propertiesFile.createNewFile();
    }
    try (OutputStream out = new FileOutputStream(propertiesFile)) {
      properties.store(out, comments);
    }
  }

}
