package com.ais.api;

import java.io.Serializable;
import java.util.Collection;

public class PostStaffInfoRequest implements Serializable {
  private static final long serialVersionUID = 1L;
  private Collection<StaffInfoBean> items;

  public Collection<StaffInfoBean> getItems() {
    return items;
  }

  public void setItems(final Collection<StaffInfoBean> items) {
    this.items = items;
  }
}
