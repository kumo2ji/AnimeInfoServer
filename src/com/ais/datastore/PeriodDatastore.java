package com.ais.datastore;

import java.util.Collection;
import java.util.List;

import com.ais.external.CoursObject;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class PeriodDatastore {
  public static void delete() {
    DatastoreUtils.delete(PeriodEntityInfo.KIND_NAME);
  }

  public static PreparedQuery query() {
    final Query query = new Query(PeriodEntityInfo.KIND_NAME);
    query.addSort(PeriodEntityInfo.YEAR_PROPERTY_NAME)
        .addSort(PeriodEntityInfo.SEASON_PROPERTY_NAME);
    return DatastoreUtils.prepare(query);
  }

  public static Query queryForYear(final long year) {
    final Query query = new Query(PeriodEntityInfo.KIND_NAME);
    final Filter filter =
        new FilterPredicate(PeriodEntityInfo.YEAR_PROPERTY_NAME, FilterOperator.EQUAL, year);
    query.setFilter(filter);
    return query;
  }

  public static Query queryForSeason(final long season) {
    final Query query = new Query(PeriodEntityInfo.KIND_NAME);
    final Filter filter =
        new FilterPredicate(PeriodEntityInfo.SEASON_PROPERTY_NAME, FilterOperator.EQUAL, season);
    query.setFilter(filter);
    return query;
  }

  public static Key queryForKey(final long year, final long season) {
    final Query query = new Query(PeriodEntityInfo.KIND_NAME);
    final Filter filter = PeriodFilter.create(year, season);
    query.setFilter(filter).setKeysOnly();
    final PreparedQuery pQuery = DatastoreUtils.prepare(query);
    return pQuery.asSingleEntity().getKey();
  }

  public static List<Key> create(final Collection<CoursObject> coursObjects) {
    final CreatePeriodRecipe recipe = new CreatePeriodRecipe(coursObjects);
    return DatastoreUtils.create(recipe);
  }
}
