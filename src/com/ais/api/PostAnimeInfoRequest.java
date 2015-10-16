package com.ais.api;

import java.io.Serializable;
import java.util.Collection;

class PostAnimeInfoRequest implements Serializable {
  private static final long serialVersionUID = 1L;
  private Collection<AnimeInfoBean> items;

  public Collection<AnimeInfoBean> getItems() {
    return items;
  }

  public void setItems(final Collection<AnimeInfoBean> items) {
    this.items = items;
  }
}
