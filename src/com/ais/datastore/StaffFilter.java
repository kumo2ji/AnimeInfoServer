package com.ais.datastore;

import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class StaffFilter {
  public static Filter create(final long animeId) {
    return new FilterPredicate(StaffEntityInfo.ANIME_ID_PROPERTY_NAME, FilterOperator.EQUAL,
        animeId);
  }
}
