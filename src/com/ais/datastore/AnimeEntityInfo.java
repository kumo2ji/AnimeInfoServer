package com.ais.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

import com.ais.api.AnimeInfoBean;
import com.ais.external.AnimeBaseObject;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class AnimeEntityInfo {
  public static final String KIND_NAME = "AnimeInfo";
  public static final String TITLE_PROPERTY_NAME = "title";
  public static final String SHORT_TITLES_PROPERTY_NAME = "shortTitles";
  public static final String PUBLIC_URL_PROPERTY_NAME = "publicUrl";
  public static final String TWITTER_ACCOUNT_PROPERTY_NAME = "twitterAccount";
  public static final String TWITTER_HASH_TAGS_PROPERTY_NAME = "twitterHashTags";
  public static final String PERIOD_KEY_PROPERTY_NAME = "periodKey";
  public static final String SEQUAL_PROPERTY_NAME = "sequel";
  public static final String SEX_PROPERTY_NAME = "sex";

  public Transformer<AnimeBaseObject, Entity> getAnimeBaseObjectToEntityTransformer() {
    return new Transformer<AnimeBaseObject, Entity>() {
      @Override
      public Entity transform(final AnimeBaseObject input) {
        final Entity entity = new Entity(KIND_NAME);
        entity.setProperty(TITLE_PROPERTY_NAME, input.getTitle());
        final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, input.getPeriodId());
        entity.setProperty(PERIOD_KEY_PROPERTY_NAME, key);
        entity.setProperty(PUBLIC_URL_PROPERTY_NAME, input.getPublic_url());
        entity.setProperty(SEQUAL_PROPERTY_NAME, input.getSequel());
        entity.setProperty(SEX_PROPERTY_NAME, input.getSex());
        final Collection<String> shortTitles = createCollection(input.getTitle_short1(),
            input.getTitle_short2(), input.getTitle_short3());
        entity.setProperty(SHORT_TITLES_PROPERTY_NAME, shortTitles);
        entity.setProperty(TWITTER_ACCOUNT_PROPERTY_NAME, input.getTwitter_account());
        final Collection<String> hashTags = createCollection(input.getTwitter_hash_tag());
        entity.setProperty(TWITTER_HASH_TAGS_PROPERTY_NAME, hashTags);
        return entity;
      }
    };
  }

  private Collection<String> createCollection(final String... values) {
    final List<String> list = Arrays.asList(values);
    return CollectionUtils.select(list, new Predicate<String>() {
      @Override
      public boolean evaluate(final String arg0) {
        return StringUtils.isNotEmpty(arg0);
      }
    });
  }

  public Transformer<AnimeInfoBean, Entity> getAnimeInfoBeanToEntityTransformer() {
    return new Transformer<AnimeInfoBean, Entity>() {
      @Override
      public Entity transform(final AnimeInfoBean input) {
        Entity entity = new Entity(KIND_NAME);
        if (input.getId() > 0) {
          final Key animeKey = KeyFactory.createKey(KIND_NAME, input.getId());
          try {
            entity = DatastoreUtils.getEntity(animeKey);
          } catch (final EntityNotFoundException e) {
            e.printStackTrace();
          }
        }

        entity.setProperty(TITLE_PROPERTY_NAME, input.getTitle());
        final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, input.getPeriodId());
        entity.setProperty(PERIOD_KEY_PROPERTY_NAME, key);
        entity.setProperty(PUBLIC_URL_PROPERTY_NAME, input.getPublicUrl());
        entity.setProperty(SEQUAL_PROPERTY_NAME, input.getSequel());
        entity.setProperty(SEX_PROPERTY_NAME, input.getSex());
        entity.setProperty(SHORT_TITLES_PROPERTY_NAME, input.getShortTitles());
        entity.setProperty(TWITTER_ACCOUNT_PROPERTY_NAME, input.getTwitterAccount());
        entity.setProperty(TWITTER_HASH_TAGS_PROPERTY_NAME, input.getTwitterHashTags());
        return entity;
      }
    };
  }

  public Transformer<Entity, AnimeInfoBean> getEntityToAnimeInfoBeanTransformer() {
    return new Transformer<Entity, AnimeInfoBean>() {
      @SuppressWarnings("unchecked")
      @Override
      public AnimeInfoBean transform(final Entity input) {
        final AnimeInfoBean bean = new AnimeInfoBean();
        bean.setId(input.getKey().getId());
        bean.setTitle((String) input.getProperty(TITLE_PROPERTY_NAME));
        Collection<String> shortTitles =
            (Collection<String>) input.getProperty(SHORT_TITLES_PROPERTY_NAME);
        if (shortTitles == null) {
          shortTitles = new ArrayList<String>();
        }
        bean.setShortTitles(shortTitles);
        bean.setPublicUrl((String) input.getProperty(PUBLIC_URL_PROPERTY_NAME));
        bean.setTwitterAccount((String) input.getProperty(TWITTER_ACCOUNT_PROPERTY_NAME));
        bean.setTwitterHashTags(
            (Collection<String>) input.getProperty(TWITTER_HASH_TAGS_PROPERTY_NAME));
        final Key key = (Key) input.getProperty(PERIOD_KEY_PROPERTY_NAME);
        bean.setPeriodId(key.getId());
        bean.setSequel((long) input.getProperty(SEQUAL_PROPERTY_NAME));
        bean.setSex((long) input.getProperty(SEX_PROPERTY_NAME));
        return bean;
      }
    };
  }
}
