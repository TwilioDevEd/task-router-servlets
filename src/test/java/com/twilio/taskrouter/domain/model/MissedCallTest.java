package com.twilio.taskrouter.domain.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MissedCallTest {

  private MissedCall reference;

  @Before
  public void setUp() {
    reference = new MissedCall("+14157234000", "ProgrammableSMS");
  }

  @Test
  public void getInternationalPhoneNumber() throws Exception {
    Assert.assertNotNull("The missed call number doesnt exist", reference.getPhoneNumber());
    Assert.assertEquals("The international format for the numbers is not the expected",
      "+1 415-723-4000",
      reference.getInternationalPhoneNumber()
    );
  }

}
