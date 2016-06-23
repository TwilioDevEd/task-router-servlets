package com.twilio.taskrouter.domain.model;

import com.twilio.taskrouter.domain.common.Utils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * A missed call from a customer, lets call them back!
 */
@Entity
@Table(name = "missed_calls")
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
      internationalPhoneNumber = Utils.formatPhoneNumberToUSInternational(phoneNumber);
    }
    return internationalPhoneNumber;
  }

  @Override
  public String toString() {
    return String.format("{ \"selected_product\": \"%s\", \"phone_number\": \"%s\" }",
      selectedProduct, phoneNumber);
  }
}
