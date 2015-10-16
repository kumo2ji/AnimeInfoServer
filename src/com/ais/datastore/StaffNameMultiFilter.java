package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.Transformer;

import com.google.appengine.api.datastore.Query.Filter;

public class StaffNameMultiFilter implements MultiFilterable<String> {
  private final Collection<String> filterValues;

  public StaffNameMultiFilter(final Collection<String> filterValues) {
    super();
    this.filterValues = filterValues;
  }

  @Override
  public String getKindName() {
    return StaffEntityInfo.KIND_NAME;
  }

  @Override
  public Collection<String> getFilterValues() {
    return filterValues;
  }

  @Override
  public Transformer<String, Filter> toFilterTransformer() {
    return new Transformer<String, Filter>() {
      @Override
      public Filter transform(final String arg0) {
        return StaffFilter.create(arg0);
      }
    };
  }

  @Override
  public int getOneTimeCount() {
    return 30;
  }
}
