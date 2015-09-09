package com.ais.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

import com.ais.api.AnimeInfoBean;
import com.ais.api.CoursObject;
import com.ais.external.AnimeBaseObject;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class DatastoreUtils {
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final AnimeEntityInfo animeEntityInfo = new AnimeEntityInfo();
  private static final CoursEntityInfo coursEntityInfo = new CoursEntityInfo();

  private DatastoreUtils() {}

  public static void deleteAnimeInfo() {
    delete(AnimeEntityInfo.KIND_NAME);
  }

  public static void deleteCoursObject() {
    delete(CoursEntityInfo.KIND_NAME);
  }

  private static void delete(final String kindName) {
    final Query query = new Query(kindName);
    query.setKeysOnly();
    final PreparedQuery pQuery = datastore.prepare(query);
    final Collection<Key> keys =
        CollectionUtils.collect(pQuery.asIterable(), new Transformer<Entity, Key>() {
          @Override
          public Key transform(final Entity arg0) {
            return arg0.getKey();
          }
        });
    datastore.delete(keys);
  }

  public static void deleteAnimeInfos(final Collection<Long> keyIds) {
    delete(AnimeEntityInfo.KIND_NAME, keyIds);
  }

  private static void delete(final String kind, final Collection<Long> keyIds) {
    final Collection<Key> keys = CollectionUtils.collect(keyIds, new Transformer<Long, Key>() {
      @Override
      public Key transform(final Long arg0) {
        return KeyFactory.createKey(kind, arg0);
      }
    });
    datastore.delete(keys);
  }

  public static List<Key> putAnimeBaseObjects(final Collection<AnimeBaseObject> baseObjects,
      final Map<String, CoursObject> coursMap) {
    final List<Entity> entitieList = queryAnimeInfoBeans(baseObjects, coursMap);
    final Collection<AnimeInfoBean> beans =
        CollectionUtils.collect(entitieList, animeEntityInfo.getEntityToAnimeInfoBeanTransformer());
    final Collection<AnimeBaseObject> selected =
        CollectionUtils.select(baseObjects, new Predicate<AnimeBaseObject>() {
          @Override
          public boolean evaluate(final AnimeBaseObject baseObject) {
            final CoursObject coursObject = coursMap.get(String.valueOf(baseObject.getCours_id()));
            return !CollectionUtils.exists(beans, new Predicate<AnimeInfoBean>() {
              @Override
              public boolean evaluate(final AnimeInfoBean bean) {
                return StringUtils.equals(baseObject.getTitle(), bean.getTitle())
                    && coursObject.equals(bean.getCoursObject());
              }
            });
          }
        });
    final Collection<Entity> entities = toEntitiesFromAnimeBaseObjects(selected);
    return datastore.put(entities);
  }

  public static List<Key> putCoursObjects(final Collection<CoursObject> coursObjects) {
    final Collection<Entity> entities = toEntitiesFromCoursObjects(coursObjects);
    return datastore.put(entities);
  }

  public static List<Key> putAnimeInfoBeans(final Collection<AnimeInfoBean> beans) {
    final Collection<Entity> entities = toEntitiesFromAnimeInfoBeans(beans);
    return datastore.put(entities);
  }

  public static PreparedQuery queryAnimeInfoBeans() {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.addSort(AnimeEntityInfo.COURS_KEY_PROPERTY_NAME)
        .addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return datastore.prepare(query);
  }

  public static Map<Key, Entity> queryAnimeInfoBean(final Collection<Key> keys) {
    return datastore.get(keys);
  }

  public static PreparedQuery queryAnimeBaseObjectsWithId(final long id) {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.setFilter(createFilterWithId(id));
    query.addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return datastore.prepare(query);
  }

  public static PreparedQuery queryAnimeInfoBeans(final long year) {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.setFilter(createFilter(year));
    query.addSort(AnimeEntityInfo.COURS_KEY_PROPERTY_NAME)
        .addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return datastore.prepare(query);
  }

  public static PreparedQuery queryAnimeInfoBeans(final long year, final long cours) {
    final Query query = new Query(AnimeEntityInfo.KIND_NAME);
    query.setFilter(createFilter(year, cours));
    query.addSort(AnimeEntityInfo.TITLE_PROPERTY_NAME);
    return datastore.prepare(query);
  }

  private static List<Entity> queryAnimeInfoBeans(
      final Collection<AnimeBaseObject> animeBaseObjects, final Map<String, CoursObject> coursMap) {
    final Collection<Filter> filters =
        CollectionUtils.collect(animeBaseObjects, new Transformer<AnimeBaseObject, Filter>() {
          @Override
          public Filter transform(final AnimeBaseObject arg0) {
            final Filter titleFilter = new FilterPredicate(AnimeEntityInfo.TITLE_PROPERTY_NAME,
                FilterOperator.EQUAL, arg0.getTitle());
            final CoursObject coursObject = coursMap.get(String.valueOf(arg0.getCours_id()));
            final Filter coursFilter = createFilter(coursObject.getYear(), coursObject.getCours());
            if (titleFilter == null || coursFilter == null) {
              return null;
            } else {
              return CompositeFilterOperator.and(titleFilter, coursFilter);
            }
          }
        });
    final Collection<Filter> filtersWithoutNull =
        CollectionUtils.select(filters, new Predicate<Filter>() {
          @Override
          public boolean evaluate(final Filter arg0) {
            return arg0 != null;
          }
        });
    return queryWithSplitFilters(filtersWithoutNull, 30,
        new Transformer<Collection<Filter>, PreparedQuery>() {
          @Override
          public PreparedQuery transform(final Collection<Filter> arg0) {
            final Filter filter = CompositeFilterOperator.or(arg0);
            final Query query = new Query(AnimeEntityInfo.KIND_NAME);
            query.setFilter(filter);
            return datastore.prepare(query);
          }
        });
  }

  private static List<Entity> queryWithSplitFilters(final Collection<Filter> filters,
      final int count, final Transformer<Collection<Filter>, PreparedQuery> transformer) {
    final List<Entity> entities = new ArrayList<Entity>();
    final int loop = (int) Math.ceil((double) filters.size() / count);
    final Iterator<Filter> iterator = filters.iterator();
    for (int i = 0; i < loop; i++) {
      final List<Filter> filterList = new ArrayList<Filter>();
      for (int j = 0; j < count && iterator.hasNext(); j++) {
        filterList.add(iterator.next());
      }
      final PreparedQuery pQuery = transformer.transform(filterList);
      CollectionUtils.addAll(entities, pQuery.asIterable());
    }
    return entities;
  }

  private static Filter createFilterWithId(final long id) {
    final Key key = KeyFactory.createKey(CoursEntityInfo.KIND_NAME, id);
    return new FilterPredicate(AnimeEntityInfo.COURS_KEY_PROPERTY_NAME, FilterOperator.EQUAL, key);
  }

  private static Filter createFilter(final long year) {
    final Query coursQuery = queryCoursObjectForYear(year);
    return createFilter(coursQuery);
  }

  private static Filter createFilter(final long year, final long cours) {
    final Query coursQuery = queryCoursObjectForYear(year);
    final Query coursQueryForCours = queryCoursObjectForCours(cours);
    final Filter coursFilter =
        CompositeFilterOperator.and(coursQuery.getFilter(), coursQueryForCours.getFilter());
    coursQuery.setFilter(coursFilter);
    return createFilter(coursQuery);
  }

  private static Filter createFilter(final Query coursQuery) {
    coursQuery.setKeysOnly();
    coursQuery.addSort(CoursEntityInfo.YEAR_PROPERTY_NAME)
        .addSort(CoursEntityInfo.COURS_PROPERTY_NAME);
    final PreparedQuery coursPQuery = datastore.prepare(coursQuery);
    final Collection<Filter> filters =
        CollectionUtils.collect(coursPQuery.asIterable(), new Transformer<Entity, Filter>() {
          @Override
          public Filter transform(final Entity arg0) {
            return new FilterPredicate(AnimeEntityInfo.COURS_KEY_PROPERTY_NAME,
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

  public static Entity getEntity(final Key key) throws EntityNotFoundException {
    return datastore.get(key);
  }

  public static Key queryCoursObject(final long year, final long cours) {
    final Filter filter = createFilter(year, cours);
    final Query query = new Query(CoursEntityInfo.KIND_NAME);
    query.setFilter(filter).setKeysOnly();
    final PreparedQuery pQuery = datastore.prepare(query);
    return pQuery.asSingleEntity().getKey();
  }

  public static PreparedQuery queryCoursObject() {
    final Query query = new Query(CoursEntityInfo.KIND_NAME);
    query.addSort(CoursEntityInfo.YEAR_PROPERTY_NAME).addSort(CoursEntityInfo.COURS_PROPERTY_NAME);
    return datastore.prepare(query);
  }

  private static Query queryCoursObjectForYear(final long year) {
    final Query query = new Query(CoursEntityInfo.KIND_NAME);
    final Filter filter =
        new FilterPredicate(CoursEntityInfo.YEAR_PROPERTY_NAME, FilterOperator.EQUAL, year);
    query.setFilter(filter);
    return query;
  }

  private static Query queryCoursObjectForCours(final long cours) {
    final Query query = new Query(CoursEntityInfo.KIND_NAME);
    final Filter filter =
        new FilterPredicate(CoursEntityInfo.COURS_PROPERTY_NAME, FilterOperator.EQUAL, cours);
    query.setFilter(filter);
    return query;
  }

  private static Collection<Entity> toEntitiesFromAnimeBaseObjects(
      final Collection<AnimeBaseObject> animeBaseObjects) {
    return CollectionUtils.collect(animeBaseObjects,
        animeEntityInfo.getAnimeBaseObjectToEntityTransformer());
  }

  private static Collection<Entity> toEntitiesFromAnimeInfoBeans(
      final Collection<AnimeInfoBean> beans) {
    return CollectionUtils.collect(beans, animeEntityInfo.getAnimeInfoBeanToEntityTransformer());
  }

  private static Collection<Entity> toEntitiesFromCoursObjects(
      final Collection<CoursObject> coursObjects) {
    return CollectionUtils.collect(coursObjects,
        coursEntityInfo.getCoursObjectToEntityTransformer());
  }
}
