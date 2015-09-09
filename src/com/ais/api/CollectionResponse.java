package com.ais.api;

import java.io.Serializable;
import java.util.Collection;

public class CollectionResponse<T> implements Serializable {
  private static final long serialVersionUID = 1L;
  private Collection<T> items;
  private String cursor;

  public Collection<T> getItems() {
    return items;
  }

  public void setItems(final Collection<T> items) {
    this.items = items;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(final String cursor) {
    this.cursor = cursor;
  }

}
