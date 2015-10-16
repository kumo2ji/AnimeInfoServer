package com.ais.datastore;

import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class PeriodFilter {
  public static Filter create(final long year, final long season) {
    final Filter yearFilter =
        new FilterPredicate(PeriodEntityInfo.YEAR_PROPERTY_NAME, FilterOperator.EQUAL, year);
    final Filter seasonFilter =
        new FilterPredicate(PeriodEntityInfo.SEASON_PROPERTY_NAME, FilterOperator.EQUAL, season);
    return CompositeFilterOperator.and(yearFilter, seasonFilter);
  }
}
