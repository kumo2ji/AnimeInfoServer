package com.ais.api;

import java.io.Serializable;
import java.util.Collection;

public class AnimeInfoBean implements Serializable {
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
  private Collection<String> directors;
  private Collection<String> writers;
  private Collection<String> musicians;
  private Collection<String> studios;

  public final long getId() {
    return id;
  }

  public final void setId(final long id) {
    this.id = id;
  }

  public final String getTitle() {
    return title;
  }

  public final void setTitle(final String title) {
    this.title = title;
  }

  public final Collection<String> getShortTitles() {
    return shortTitles;
  }

  public final void setShortTitles(final Collection<String> shortTitles) {
    this.shortTitles = shortTitles;
  }

  public final String getPublicUrl() {
    return publicUrl;
  }

  public final void setPublicUrl(final String publicUrl) {
    this.publicUrl = publicUrl;
  }

  public final String getTwitterAccount() {
    return twitterAccount;
  }

  public final void setTwitterAccount(final String twitterAccount) {
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

  public final long getSex() {
    return sex;
  }

  public final void setSex(final long sex) {
    this.sex = sex;
  }

  public final long getSequel() {
    return sequel;
  }

  public final void setSequel(final long sequel) {
    this.sequel = sequel;
  }

  public String getWikiTitle() {
    return wikiTitle;
  }

  public void setWikiTitle(final String wikiTitle) {
    this.wikiTitle = wikiTitle;
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
    return "AnimeInfoBean [id=" + id + ", title=" + title + ", shortTitles=" + shortTitles
        + ", publicUrl=" + publicUrl + ", twitterAccount=" + twitterAccount + ", twitterHashTags="
        + twitterHashTags + ", periodId=" + periodId + ", sex=" + sex + ", sequel=" + sequel
        + ", wikiTitle=" + wikiTitle + ", directors=" + directors + ", writers=" + writers
        + ", musicians=" + musicians + ", studios=" + studios + "]";
  }

}
