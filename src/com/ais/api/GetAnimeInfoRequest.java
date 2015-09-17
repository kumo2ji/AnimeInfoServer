package com.ais.api;

import java.io.Serializable;

public class GetAnimeInfoRequest implements Serializable {
  private static final long serialVersionUID = 1L;
  private PeriodBean periodBean;
  private int limit;
  private String cursor;

  public PeriodBean getPeriodBean() {
    return periodBean;
  }

  public void setPeriodBean(final PeriodBean periodBean) {
    this.periodBean = periodBean;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(final int limit) {
    this.limit = limit;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(final String cursor) {
    this.cursor = cursor;
  }

}
