package com.ais.datastore;

import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class StaffFilter {
  public static Filter create(final String name) {
    return new FilterPredicate(StaffEntityInfo.NAME_PROPERTY_NAME, FilterOperator.EQUAL, name);
  }
}
