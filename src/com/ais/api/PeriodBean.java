package com.ais.api;

import java.io.Serializable;

public class PeriodBean implements Serializable {
  private static final long serialVersionUID = 1L;
  private long id;
  private long year;
  private long season;

  public long getId() {
    return id;
  }

  public void setId(final long id) {
    this.id = id;
  }

  public long getYear() {
    return year;
  }

  public void setYear(final long year) {
    this.year = year;
  }

  public long getSeason() {
    return season;
  }

  public void setSeason(final long season) {
    this.season = season;
  }

  @Override
  public String toString() {
    return "PeriodBean [id=" + id + ", year=" + year + ", season=" + season + "]";
  }
}
