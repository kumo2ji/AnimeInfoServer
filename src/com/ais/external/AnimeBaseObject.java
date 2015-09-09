package com.ais.external;

public final class AnimeBaseObject {
  private long id;
  private String title;
  private String title_short1;
  private String title_short2;
  private String title_short3;
  private String public_url;
  private String twitter_account;
  private String twitter_hash_tag;
  private long cours_id;
  private String created_at;
  private String updated_at;
  private long sex;
  private long sequel;

  public AnimeBaseObject() {
    super();
  }

  public long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getTitle_short1() {
    return title_short1;
  }

  public String getTitle_short2() {
    return title_short2;
  }

  public String getTitle_short3() {
    return title_short3;
  }

  public String getPublic_url() {
    return public_url;
  }

  public String getTwitter_account() {
    return twitter_account;
  }

  public String getTwitter_hash_tag() {
    return twitter_hash_tag;
  }

  public long getCours_id() {
    return cours_id;
  }

  public String getCreated_at() {
    return created_at;
  }

  public String getUpdated_at() {
    return updated_at;
  }

  public long getSex() {
    return sex;
  }

  public long getSequel() {
    return sequel;
  }

  @Override
  public String toString() {
    return "AnimeBaseObject [id=" + id + ", title=" + title + ", title_short1=" + title_short1
        + ", title_short2=" + title_short2 + ", title_short3=" + title_short3 + ", public_url="
        + public_url + ", twitter_account=" + twitter_account + ", twitter_hash_tag="
        + twitter_hash_tag + ", cours_id=" + cours_id + ", created_at=" + created_at
        + ", updated_at=" + updated_at + ", sex=" + sex + ", sequel=" + sequel + "]";
  }
}
