package com.ais.datastore;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

import com.ais.api.AnimeInfoBean;
import com.ais.external.AnimeBaseObject;
import com.ais.external.CoursObject;
import com.ais.utils.BiFunc;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class AnimeDatastore {
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final AnimeEntityInfo animeInfo = new AnimeEntityInfo();

  public static void delete() {
    DatastoreUtils.delete(AnimeEntityInfo.KIND_NAME);
  }

  public static void delete(final Collection<Long> keyIds) {
    DatastoreUtils.delete(AnimeEntityInfo.KIND_NAME, keyIds);
  }

  public static List<Key> putAnimeBaseObjects(final Collection<AnimeBaseObject> baseObjects) {
    return DatastoreUtils.put(AnimeEntityInfo.KIND_NAME, baseObjects, getToFilterTransformer(),
        animeInfo.getEntityToAnimeInfoBeanTransformer(), getEqualFunc(),
        new Transformer<AnimeBaseObject, Entity>() {
          @Override
          public Entity transform(final AnimeBaseObject arg0) {
            final CoursObject coursObject = arg0.getCoursObject();
            final Key key =
                PeriodDatastore.queryForKey(coursObject.getYear(), coursObject.getCours());
            arg0.setPeriodId(key.getId());
            return animeInfo.getAnimeBaseObjectToEntityTransformer().transform(arg0);
          }
        });
  }

  private static BiFunc<AnimeBaseObject, AnimeInfoBean, Boolean> getEqualFunc() {
    return new BiFunc<AnimeBaseObject, AnimeInfoBean, Boolean>() {
      @Override
      public Boolean apply(final AnimeBaseObject arg1, final AnimeInfoBean arg2) {
        return StringUtils.equals(arg1.getTitle(), arg2.getTitle());
      }
    };
  }

  public static List<Key> put(final Collection<AnimeInfoBean> beans) {
    final Collection<Entity> entities = toEntitiesFromAnimeInfoBeans(beans);
    return datastore.put(entities);
  }

  private static Transformer<AnimeBaseObject, Filter> getToFilterTransformer() {
    return new Transformer<AnimeBaseObject, Filter>() {
      @Override
      public Filter transform(final AnimeBaseObject arg0) {
        final Filter titleFilter = new FilterPredicate(AnimeEntityInfo.TITLE_PROPERTY_NAME,
            FilterOperator.EQUAL, arg0.getTitle());
        final CoursObject coursObject = arg0.getCoursObject();
        final Filter coursFilter = createFilter(coursObject.getYear(), coursObject.getCours());
        if (titleFilter == null || coursFilter == null) {
          return null;
        } else {
          return CompositeFilterOperator.and(titleFilter, coursFilter);
        }
      }
    };
  }

  private static Filter createFilter(final long year) {
    final Query coursQuery = PeriodDatastore.queryForYear(year);
    return createFilter(coursQuery);
  }

  private static Filter createFilter(final long year, final long cours) {
    final Query coursQuery = PeriodDatastore.queryForYear(year);
    final Query coursQueryForCours = PeriodDatastore.queryForSeason(cours);
    final Filter coursFilter =
        CompositeFilterOperator.and(coursQuery.getFilter(), coursQueryForCours.getFilter());
    coursQuery.setFilter(coursFilter);
    return createFilter(coursQuery);
  }

  private static Filter createFilter(final Query coursQuery) {
    coursQuery.setKeysOnly();
    coursQuery.addSort(PeriodEntityInfo.YEAR_PROPERTY_NAME)
        .addSort(PeriodEntityInfo.SEASON_PROPERTY_NAME);
    final PreparedQuery coursPQuery = datastore.prepare(coursQuery);
    final Collection<Filter> filters =
        CollectionUtils.collect(coursPQuery.asIterable(), new Transformer<Entity, Filter>() {
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

  public static PreparedQuery query() {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.addSort(AnimeEntityInfo.PERIOD_KEY_PROPERTY_NAME)
        .addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return datastore.prepare(query);
  }

  public static Map<Key, Entity> query(final Collection<Key> keys) {
    return datastore.get(keys);
  }

  public static PreparedQuery queryWithPeriodId(final long id) {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.setFilter(createFilterWithPeriodId(id));
    query.addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return datastore.prepare(query);
  }

  public static PreparedQuery query(final long year) {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.setFilter(createFilter(year));
    query.addSort(AnimeEntityInfo.PERIOD_KEY_PROPERTY_NAME)
        .addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return datastore.prepare(query);
  }

  public static PreparedQuery query(final long year, final long cours) {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.setFilter(createFilter(year, cours));
    query.addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return datastore.prepare(query);
  }

  private static Collection<Entity> toEntitiesFromAnimeInfoBeans(
      final Collection<AnimeInfoBean> beans) {
    return CollectionUtils.collect(beans, animeInfo.getAnimeInfoBeanToEntityTransformer());
  }

  private static Filter createFilterWithPeriodId(final long id) {
    final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, id);
    return new FilterPredicate(AnimeEntityInfo.PERIOD_KEY_PROPERTY_NAME, FilterOperator.EQUAL, key);
  }
}
