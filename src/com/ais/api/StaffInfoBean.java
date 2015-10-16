package com.ais.api;

import java.io.Serializable;

public class StaffInfoBean implements Serializable {
  private static final long serialVersionUID = 1L;
  private long id;
  private String name;

  public long getId() {
    return id;
  }

  public void setId(final long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "StaffInfoBean [id=" + id + ", name=" + name + "]";
  }
}
