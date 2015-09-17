package com.ais.external;

import java.io.Serializable;

public final class CoursObject implements Serializable {
  private static final long serialVersionUID = 1L;
  private long id;
  private long year;
  private long cours;

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


  public long getCours() {
    return cours;
  }


  public void setCours(final long cours) {
    this.cours = cours;
  }


  @Override
  public String toString() {
    return "CoursObject [id=" + id + ", year=" + year + ", cours=" + cours + "]";
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (cours ^ (cours >>> 32));
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + (int) (year ^ (year >>> 32));
    return result;
  }


  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CoursObject other = (CoursObject) obj;
    if (cours != other.cours) {
      return false;
    }
    if (id != other.id) {
      return false;
    }
    if (year != other.year) {
      return false;
    }
    return true;
  }

}
