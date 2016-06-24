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

  private MissedCall() {
  }

  public MissedCall(String phoneNumber, String selectedProduct) {
    this.created = new Date();
    this.selectedProduct = selectedProduct;
    this.phoneNumber = new PhoneNumber(phoneNumber);
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

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getPhoneNumber() {
    return phoneNumber.getPhoneNumber();
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber.setPhoneNumber(phoneNumber);
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
