package com.twilio.taskrouter.domain.error;

/**
* Exception for handled errors of the project
*/
public class TaskRouterException extends RuntimeException {

  public TaskRouterException(Throwable ex) {
    super("Unexpected exception: " + ex.getMessage(), ex);
  }

  public TaskRouterException(String message) {
    super(message);
  }
}
