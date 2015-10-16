package com.ais.datastore;

import java.io.Serializable;
import java.util.Collection;

public class AnimeDatum implements Serializable {
  private static final long serialVersionUID = 1L;
  private long id;
  private String title;
  private Collection<String> shortTitles;
  private String publicUrl;
  private String twitterAccount;
  private Collection<String> twitterHashTags;
  private long periodId;
  private long sex;
  private long sequel;
  private String wikiTitle;
  private Collection<Long> directors;
  private Collection<Long> writers;
  private Collection<Long> musicians;
  private Collection<Long> studios;

  public long getId() {
    return id;
  }

  public void setId(final long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public Collection<String> getShortTitles() {
    return shortTitles;
  }

  public void setShortTitles(final Collection<String> shortTitles) {
    this.shortTitles = shortTitles;
  }

  public String getPublicUrl() {
    return publicUrl;
  }

  public void setPublicUrl(final String publicUrl) {
    this.publicUrl = publicUrl;
  }

  public String getTwitterAccount() {
    return twitterAccount;
  }

  public void setTwitterAccount(final String twitterAccount) {
    this.twitterAccount = twitterAccount;
  }

  public Collection<String> getTwitterHashTags() {
    return twitterHashTags;
  }

  public void setTwitterHashTags(final Collection<String> twitterHashTags) {
    this.twitterHashTags = twitterHashTags;
  }

  public long getPeriodId() {
    return periodId;
  }

  public void setPeriodId(final long periodId) {
    this.periodId = periodId;
  }

  public long getSex() {
    return sex;
  }

  public void setSex(final long sex) {
    this.sex = sex;
  }

  public long getSequel() {
    return sequel;
  }

  public void setSequel(final long sequel) {
    this.sequel = sequel;
  }

  public String getWikiTitle() {
    return wikiTitle;
  }

  public void setWikiTitle(final String wikiTitle) {
    this.wikiTitle = wikiTitle;
  }

  public Collection<Long> getDirectors() {
    return directors;
  }

  public void setDirectors(final Collection<Long> directors) {
    this.directors = directors;
  }

  public Collection<Long> getWriters() {
    return writers;
  }

  public void setWriters(final Collection<Long> writers) {
    this.writers = writers;
  }

  public Collection<Long> getMusicians() {
    return musicians;
  }

  public void setMusicians(final Collection<Long> musicians) {
    this.musicians = musicians;
  }

  public Collection<Long> getStudios() {
    return studios;
  }

  public void setStudios(final Collection<Long> studios) {
    this.studios = studios;
  }
}
