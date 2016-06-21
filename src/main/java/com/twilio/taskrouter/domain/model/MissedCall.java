package com.twilio.taskrouter.domain.model;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.twilio.taskrouter.domain.error.TaskRouterException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * A missed call from a customer, lets call them back!
 */
@Entity
public final class MissedCall {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Size(max = 30)
  private String selectedProduct;

  @Size(max = 30)
  private String phoneNumber;

  private Date created;

  @Transient
  private String internationalPhoneNumber;

  private MissedCall() {
    this.created = new Date();
  }

  public MissedCall(String phoneNumber, String selectedProduct) {
    this();
    this.selectedProduct = selectedProduct;
    this.phoneNumber = phoneNumber;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSelectedProduct() {
    return selectedProduct;
  }

  public void setSelectedProduct(String selectedProduct) {
    this.selectedProduct = selectedProduct;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getInternationalPhoneNumber() {
    if (internationalPhoneNumber == null) {
      PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
      try {
        Phonenumber.PhoneNumber usPhoneNumber = phoneUtil.parse(phoneNumber, "US");
        internationalPhoneNumber = phoneUtil.format(usPhoneNumber,
          PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
      } catch (NumberParseException e) {
        throw new TaskRouterException("Invalid phone format: " + e.toString());
      }
    }
    return internationalPhoneNumber;
  }

  @Override
  public String toString() {
    return selectedProduct;
  }
}
