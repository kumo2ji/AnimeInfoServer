package com.ais.datastore;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.ais.api.AnimeInfoBean;
import com.ais.external.AnimeBaseObject;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class AnimeDatastore {
  public static void delete() {
    DatastoreUtils.delete(AnimeEntityInfo.KIND_NAME);
  }

  public static void delete(final Collection<Long> keyIds) {
    DatastoreUtils.delete(AnimeEntityInfo.KIND_NAME, keyIds);
  }

  public static List<Key> create(final Collection<AnimeBaseObject> baseObjects) {
    final CreateAnimeRecipe recipe = new CreateAnimeRecipe(baseObjects);
    return DatastoreUtils.create(recipe);
  }

  public static List<Key> put(final Collection<AnimeInfoBean> beans) {
    final Collection<Entity> entities =
        CollectionUtils.collect(beans, AnimeEntityInfo.getAnimeInfoBeanToEntityTransformer());
    return DatastoreUtils.put(entities);
  }

  public static PreparedQuery query() {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return DatastoreUtils.prepare(query);
  }

  public static Map<Key, Entity> query(final Collection<Key> keys) {
    return DatastoreUtils.get(keys);
  }

  public static PreparedQuery queryWithPeriodId(final long id) {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.setFilter(AnimeFilter.createWithPeriodId(id));
    query.addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return DatastoreUtils.prepare(query);
  }

  public static PreparedQuery query(final long year) {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.setFilter(AnimeFilter.create(year));
    query.addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return DatastoreUtils.prepare(query);
  }

  public static PreparedQuery query(final long year, final long cours) {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.setFilter(AnimeFilter.create(year, cours));
    query.addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return DatastoreUtils.prepare(query);
  }
}
