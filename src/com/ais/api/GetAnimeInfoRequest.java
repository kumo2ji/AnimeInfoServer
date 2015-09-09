package com.ais.api;

import java.io.Serializable;

public class GetAnimeInfoRequest implements Serializable {
  private static final long serialVersionUID = 1L;
  private CoursObject coursObject;
  private int limit;
  private String cursor;

  public CoursObject getCoursObject() {
    return coursObject;
  }

  public void setCoursObject(final CoursObject coursObject) {
    this.coursObject = coursObject;
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
