package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.Transformer;

import com.google.appengine.api.datastore.Query.Filter;

public class StaffAnimeIdMultiFilter implements MultiFilterable<Long> {
  private final Collection<Long> filterValues;

  public StaffAnimeIdMultiFilter(final Collection<Long> filterValues) {
    super();
    this.filterValues = filterValues;
  }

  @Override
  public String getKindName() {
    return StaffEntityInfo.KIND_NAME;
  }

  @Override
  public Collection<Long> getFilterValues() {
    return filterValues;
  }

  @Override
  public Transformer<Long, Filter> toFilterTransformer() {
    return new Transformer<Long, Filter>() {
      @Override
      public Filter transform(final Long arg0) {
        return StaffFilter.create(arg0);
      }
    };
  }

  @Override
  public int getOneTimeCount() {
    return 30;
  }

}
