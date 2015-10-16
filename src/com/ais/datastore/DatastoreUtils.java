package com.ais.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;

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

public class DatastoreUtils {
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private DatastoreUtils() {}

  public static PreparedQuery prepare(final Query query) {
    return datastore.prepare(query);
  }

  public static List<Key> put(final Iterable<Entity> entities) {
    return datastore.put(entities);
  }

  public static Map<Key, Entity> get(final Iterable<Key> keys) {
    return datastore.get(keys);
  }

  public static void delete(final String kindName) {
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

  public static void delete(final String kind, final Collection<Long> keyIds) {
    final Collection<Key> keys = CollectionUtils.collect(keyIds, new Transformer<Long, Key>() {
      @Override
      public Key transform(final Long arg0) {
        return KeyFactory.createKey(kind, arg0);
      }
    });
    datastore.delete(keys);
  }

  public static Entity get(final Key key) throws EntityNotFoundException {
    return datastore.get(key);
  }

  public static <T, K> List<Key> create(final Creatable<T, K> recipe) {
    final Collection<Entity> storedEntities = query(recipe.getMultiFilter());
    final Collection<K> beans = CollectionUtils.collect(storedEntities, recipe.toBeanTransformer());
    final Collection<T> objectsWithoutSame =
        CollectionUtils.select(recipe.getTargets(), new Predicate<T>() {
          @Override
          public boolean evaluate(final T baseObject) {
            return !CollectionUtils.exists(beans, new Predicate<K>() {
              @Override
              public boolean evaluate(final K arg0) {
                return recipe.equals(baseObject, arg0);
              }
            });
          }
        });
    final Collection<Entity> entities =
        CollectionUtils.collect(objectsWithoutSame, recipe.toEntityTransformer());
    return datastore.put(entities);
  }

  private static List<Entity> query(final Collection<Filter> filters, final int count,
      final Transformer<Collection<Filter>, PreparedQuery> transformer) {
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

  public static <T> List<Entity> query(final MultiFilterable<T> multiFilter) {
    final Collection<Filter> filters =
        CollectionUtils.collect(multiFilter.getFilterValues(), multiFilter.toFilterTransformer());
    final Collection<Filter> filtersWithoutNull =
        CollectionUtils.select(filters, new Predicate<Filter>() {
          @Override
          public boolean evaluate(final Filter arg0) {
            return arg0 != null;
          }
        });
    return query(filtersWithoutNull, multiFilter.getOneTimeCount(),
        filtersToPreparedQuery(multiFilter.getKindName()));
  }

  private static Transformer<Collection<Filter>, PreparedQuery> filtersToPreparedQuery(
      final String kindName) {
    return new Transformer<Collection<Filter>, PreparedQuery>() {
      @Override
      public PreparedQuery transform(final Collection<Filter> arg0) {
        final Filter filter = CompositeFilterOperator.or(arg0);
        final Query query = new Query(kindName);
        query.setFilter(filter);
        return datastore.prepare(query);
      }
    };
  }
}
