package com.ais.api;

import java.util.Collection;

public class IdRequest {
  private Collection<Long> ids;

  public Collection<Long> getIds() {
    return ids;
  }

  public void setIds(final Collection<Long> ids) {
    this.ids = ids;
  }

}
