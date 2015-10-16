package com.ais.api;

import java.io.Serializable;
import java.util.Collection;

public class StaffInfoBean implements Serializable {
  private static final long serialVersionUID = 1L;
  private long id;
  private long animeId;
  private Collection<String> directors;
  private Collection<String> writers;
  private Collection<String> musicians;
  private Collection<String> studios;

  public long getId() {
    return id;
  }

  public void setId(final long id) {
    this.id = id;
  }

  public long getAnimeId() {
    return animeId;
  }

  public void setAnimeId(final long animeId) {
    this.animeId = animeId;
  }

  public Collection<String> getDirectors() {
    return directors;
  }

  public void setDirectors(final Collection<String> directors) {
    this.directors = directors;
  }

  public Collection<String> getWriters() {
    return writers;
  }

  public void setWriters(final Collection<String> writers) {
    this.writers = writers;
  }

  public Collection<String> getMusicians() {
    return musicians;
  }

  public void setMusicians(final Collection<String> musicians) {
    this.musicians = musicians;
  }

  public Collection<String> getStudios() {
    return studios;
  }

  public void setStudios(final Collection<String> studios) {
    this.studios = studios;
  }

  @Override
  public String toString() {
    return "StaffInfoBean [id=" + id + ", animeId=" + animeId + ", directors=" + directors
        + ", writers=" + writers + ", musicians=" + musicians + ", studios=" + studios + "]";
  }
}
