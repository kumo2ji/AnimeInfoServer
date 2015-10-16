package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class AnimeFilter {
  public static Filter create(final long year) {
    final Query coursQuery = PeriodDatastore.queryForYear(year);
    return create(coursQuery);
  }

  public static Filter create(final long year, final long season) {
    final Query periodQuery = PeriodDatastore.queryForYear(year);
    final Query seasonQuery = PeriodDatastore.queryForSeason(season);
    final Filter coursFilter =
        CompositeFilterOperator.and(periodQuery.getFilter(), seasonQuery.getFilter());
    periodQuery.setFilter(coursFilter);
    return create(periodQuery);
  }

  private static Filter create(final Query periodQuery) {
    periodQuery.setKeysOnly();
    periodQuery.addSort(PeriodEntityInfo.YEAR_PROPERTY_NAME)
        .addSort(PeriodEntityInfo.SEASON_PROPERTY_NAME);
    final PreparedQuery periodPQuery = DatastoreUtils.prepare(periodQuery);
    final Collection<Filter> filters =
        CollectionUtils.collect(periodPQuery.asIterable(), new Transformer<Entity, Filter>() {
          @Override
          public Filter transform(final Entity arg0) {
            return new FilterPredicate(AnimeEntityInfo.PERIOD_KEY_PROPERTY_NAME,
                FilterOperator.EQUAL, arg0.getKey());
          }
        });
    if (filters.isEmpty()) {
      return null;
    } else if (filters.size() == 1) {
      return filters.iterator().next();
    } else {
      return CompositeFilterOperator.or(filters);
    }
  }

  public static Filter createWithPeriodId(final long id) {
    final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, id);
    return new FilterPredicate(AnimeEntityInfo.PERIOD_KEY_PROPERTY_NAME, FilterOperator.EQUAL, key);
  }
}
