package com.twilio.taskrouter.domain.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * A missed call from a customer, lets call them back!
 */
@Entity
@Table(name = "missed_calls")
public final class MissedCall {

  @Id
  @GeneratedValue
  private Long id;

  @Size(max = 30)
  private String selectedProduct;

  @Embedded
  private PhoneNumber phoneNumber;

  private Date created;

  /**
   * For JPA purposes is package protected
   * Avoids initialization by this constructor
   */
  protected MissedCall() {
  }

  public MissedCall(String phoneNumber, String selectedProduct) {
    this.created = new Date();
    this.selectedProduct = selectedProduct;
    this.phoneNumber = new PhoneNumber(phoneNumber);
  }

  public String getSelectedProduct() {
    return selectedProduct;
  }

  public String getPhoneNumber() {
    return phoneNumber.getPhoneNumber();
  }

  public String getInternationalPhoneNumber() {
    return phoneNumber.getInternationalPhoneNumber();
  }

  @Override
  public String toString() {
    return String.format("{ \"selected_product\": \"%s\", \"phone_number\": \"%s\" }",
      getSelectedProduct(), getPhoneNumber());
  }
}
