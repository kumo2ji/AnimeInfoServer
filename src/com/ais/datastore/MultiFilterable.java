package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.Transformer;

import com.google.appengine.api.datastore.Query.Filter;

public interface MultiFilterable<T> {
  String getKindName();

  Collection<T> getFilterValues();

  Transformer<T, Filter> toFilterTransformer();

  int getOneTimeCount();
}
