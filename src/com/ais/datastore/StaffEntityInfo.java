package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.Transformer;

import com.ais.api.StaffInfoBean;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;

public class StaffEntityInfo {
  public static final String KIND_NAME = "StaffInfo";
  public static final String ANIME_ID_PROPERTY_NAME = "animeId";
  public static final String WIKI_TITLE_PROPERTY_NAME = "wikiTitle";
  public static final String DIRECTORS_PROPERTY_NAME = "directors";
  public static final String WRITERS_PROPERTY_NAME = "writers";
  public static final String MUSICIANS_PROPERTY_NAME = "musicians";
  public static final String STUDIOS_PROPERTY_NAME = "studios";

  public static Transformer<StaffInfoBean, Entity> beanToEntity() {
    return new Transformer<StaffInfoBean, Entity>() {
      @Override
      public Entity transform(final StaffInfoBean arg0) {
        final Entity entity = new Entity(KeyFactory.createKey(KIND_NAME, arg0.getId()));
        entity.setProperty(ANIME_ID_PROPERTY_NAME, arg0.getAnimeId());
        entity.setProperty(DIRECTORS_PROPERTY_NAME, arg0.getDirectors());
        entity.setProperty(WRITERS_PROPERTY_NAME, arg0.getWriters());
        entity.setProperty(MUSICIANS_PROPERTY_NAME, arg0.getMusicians());
        entity.setProperty(STUDIOS_PROPERTY_NAME, arg0.getStudios());
        return entity;
      }
    };
  }

  public static Transformer<Entity, StaffInfoBean> entityToBean() {
    return new Transformer<Entity, StaffInfoBean>() {
      @SuppressWarnings("unchecked")
      @Override
      public StaffInfoBean transform(final Entity arg0) {
        final StaffInfoBean bean = new StaffInfoBean();
        bean.setId(arg0.getKey().getId());
        bean.setAnimeId((long) arg0.getProperty(ANIME_ID_PROPERTY_NAME));
        bean.setDirectors((Collection<String>) arg0.getProperty(DIRECTORS_PROPERTY_NAME));
        bean.setWriters((Collection<String>) arg0.getProperty(WRITERS_PROPERTY_NAME));
        bean.setMusicians((Collection<String>) arg0.getProperty(MUSICIANS_PROPERTY_NAME));
        bean.setStudios((Collection<String>) arg0.getProperty(STUDIOS_PROPERTY_NAME));
        return bean;
      }
    };
  }
}
