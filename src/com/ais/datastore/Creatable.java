package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.Transformer;

import com.google.appengine.api.datastore.Entity;

public interface Creatable<T, K> {
  Collection<T> getTargets();

  Transformer<Entity, K> toBeanTransformer();

  Boolean equals(T t, K k);

  Transformer<T, Entity> toEntityTransformer();

  MultiFilterable<T> getMultiFilter();
}
