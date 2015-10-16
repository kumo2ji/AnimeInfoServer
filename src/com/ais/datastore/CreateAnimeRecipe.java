package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

import com.ais.api.AnimeInfoBean;
import com.ais.external.AnimeBaseObject;
import com.ais.external.CoursObject;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class CreateAnimeRecipe implements Creatable<AnimeBaseObject, AnimeInfoBean> {
  private final Collection<AnimeBaseObject> targets;

  public CreateAnimeRecipe(final Collection<AnimeBaseObject> targets) {
    super();
    this.targets = targets;
  }

  @Override
  public Collection<AnimeBaseObject> getTargets() {
    return targets;
  }

  @Override
  public Transformer<Entity, AnimeInfoBean> toBeanTransformer() {
    return AnimeEntityInfo.getEntityToAnimeInfoBeanTransformer();
  }

  @Override
  public Boolean equals(final AnimeBaseObject object, final AnimeInfoBean bean) {
    return StringUtils.equals(object.getTitle(), bean.getTitle());
  }

  @Override
  public Transformer<AnimeBaseObject, Entity> toEntityTransformer() {
    return new Transformer<AnimeBaseObject, Entity>() {
      @Override
      public Entity transform(final AnimeBaseObject arg0) {
        final CoursObject coursObject = arg0.getCoursObject();
        final Key key = PeriodDatastore.queryForKey(coursObject.getYear(), coursObject.getCours());
        arg0.setPeriodId(key.getId());
        return AnimeEntityInfo.getAnimeBaseObjectToEntityTransformer().transform(arg0);
      }
    };
  }

  @Override
  public MultiFilterable<AnimeBaseObject> getMultiFilter() {
    return new MultiFilter(targets);
  }

  private static class MultiFilter implements MultiFilterable<AnimeBaseObject> {
    private final Collection<AnimeBaseObject> filterValues;

    public MultiFilter(final Collection<AnimeBaseObject> filterValues) {
      super();
      this.filterValues = filterValues;
    }

    @Override
    public String getKindName() {
      return AnimeEntityInfo.KIND_NAME;
    }

    @Override
    public Collection<AnimeBaseObject> getFilterValues() {
      return filterValues;
    }

    @Override
    public Transformer<AnimeBaseObject, Filter> toFilterTransformer() {
      return new Transformer<AnimeBaseObject, Filter>() {
        @Override
        public Filter transform(final AnimeBaseObject arg0) {
          final Filter titleFilter = new FilterPredicate(AnimeEntityInfo.TITLE_PROPERTY_NAME,
              FilterOperator.EQUAL, arg0.getTitle());
          final CoursObject coursObject = arg0.getCoursObject();
          final Filter coursFilter =
              AnimeFilter.create(coursObject.getYear(), coursObject.getCours());
          if (titleFilter == null || coursFilter == null) {
            return null;
          } else {
            return CompositeFilterOperator.and(titleFilter, coursFilter);
          }
        }
      };
    }

    @Override
    public int getOneTimeCount() {
      return 30;
    }

  }

}
