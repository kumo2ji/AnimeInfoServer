package com.ais.api;

import java.io.Serializable;

public class GetAnimeInfoRequest implements Serializable {
  private static final long serialVersionUID = 1L;
  private PeriodBean period;
  private int limit;
  private String cursor;

  public PeriodBean getPeriod() {
    return period;
  }

  public void setPeriod(final PeriodBean periodBean) {
    this.period = periodBean;
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
