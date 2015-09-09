package com.ais.api;

import java.io.Serializable;

public class BooleanResponse implements Serializable {
  private static final long serialVersionUID = 1L;
  private boolean value;
  private String message;

  public BooleanResponse(final boolean value) {
    super();
    this.value = value;
  }

  public BooleanResponse(final boolean value, final String message) {
    super();
    this.value = value;
    this.message = message;
  }

  public boolean getValue() {
    return value;
  }

  public void setValue(final boolean value) {
    this.value = value;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
