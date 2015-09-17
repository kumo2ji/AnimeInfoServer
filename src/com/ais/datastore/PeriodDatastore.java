package com.ais.datastore;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.Transformer;

import com.ais.api.PeriodBean;
import com.ais.external.CoursObject;
import com.ais.utils.BiFunc;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class PeriodDatastore {
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final PeriodEntityInfo periodInfo = new PeriodEntityInfo();

  public static void delete() {
    DatastoreUtils.delete(PeriodEntityInfo.KIND_NAME);
  }

  public static PreparedQuery query() {
    final Query query = new Query(PeriodEntityInfo.KIND_NAME);
    query.addSort(PeriodEntityInfo.YEAR_PROPERTY_NAME)
        .addSort(PeriodEntityInfo.SEASON_PROPERTY_NAME);
    return datastore.prepare(query);
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
    final Filter filter = createFilter(year, season);
    query.setFilter(filter).setKeysOnly();
    final PreparedQuery pQuery = datastore.prepare(query);
    return pQuery.asSingleEntity().getKey();
  }

  private static Filter createFilter(final long year, final long season) {
    final Filter yearFilter =
        new FilterPredicate(PeriodEntityInfo.YEAR_PROPERTY_NAME, FilterOperator.EQUAL, year);
    final Filter seasonFilter =
        new FilterPredicate(PeriodEntityInfo.SEASON_PROPERTY_NAME, FilterOperator.EQUAL, season);
    return CompositeFilterOperator.and(yearFilter, seasonFilter);
  }

  public static List<Key> put(final Collection<CoursObject> coursObjects) {
    return DatastoreUtils.put(PeriodEntityInfo.KIND_NAME, coursObjects, getToFilterTransformer(),
        periodInfo.getEntityToPeriodBeanTransformer(), getEqualFunc(),
        periodInfo.getCoursObjectToEntityTransformer());
  }

  private static BiFunc<CoursObject, PeriodBean, Boolean> getEqualFunc() {
    return new BiFunc<CoursObject, PeriodBean, Boolean>() {
      @Override
      public Boolean apply(final CoursObject arg1, final PeriodBean arg2) {
        return arg1.getYear() == arg2.getYear() && arg1.getCours() == arg2.getSeason();
      }
    };
  }

  private static Transformer<CoursObject, Filter> getToFilterTransformer() {
    return new Transformer<CoursObject, Filter>() {
      @Override
      public Filter transform(final CoursObject arg0) {
        return createFilter(arg0.getYear(), arg0.getCours());
      }
    };
  }
}
