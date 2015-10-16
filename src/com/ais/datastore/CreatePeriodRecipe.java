package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.Transformer;

import com.ais.api.PeriodBean;
import com.ais.external.CoursObject;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.Filter;

public class CreatePeriodRecipe implements Creatable<CoursObject, PeriodBean> {
  private final Collection<CoursObject> targets;

  public CreatePeriodRecipe(final Collection<CoursObject> targets) {
    super();
    this.targets = targets;
  }

  @Override
  public Collection<CoursObject> getTargets() {
    return targets;
  }

  @Override
  public Transformer<Entity, PeriodBean> toBeanTransformer() {
    return PeriodEntityInfo.getEntityToPeriodBeanTransformer();
  }

  @Override
  public Boolean equals(final CoursObject t, final PeriodBean k) {
    return t.getYear() == k.getYear() && t.getCours() == k.getSeason();
  }

  @Override
  public Transformer<CoursObject, Entity> toEntityTransformer() {
    return PeriodEntityInfo.getCoursObjectToEntityTransformer();
  }

  @Override
  public MultiFilterable<CoursObject> getMultiFilter() {
    return new MultiFilter(targets);
  }

  private static class MultiFilter implements MultiFilterable<CoursObject> {
    private final Collection<CoursObject> filterValues;

    public MultiFilter(final Collection<CoursObject> filterValues) {
      super();
      this.filterValues = filterValues;
    }

    @Override
    public String getKindName() {
      return PeriodEntityInfo.KIND_NAME;
    }

    @Override
    public Collection<CoursObject> getFilterValues() {
      return filterValues;
    }

    @Override
    public Transformer<CoursObject, Filter> toFilterTransformer() {
      return new Transformer<CoursObject, Filter>() {
        @Override
        public Filter transform(final CoursObject arg0) {
          return PeriodFilter.create(arg0.getYear(), arg0.getCours());
        }
      };
    }

    @Override
    public int getOneTimeCount() {
      return 30;
    }

  }
}
