package com.ais.datastore;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.ais.api.StaffInfoBean;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class StaffDatastore {
  public static List<Key> create(final Collection<String> names) {
    final CreateStaffRecipe recipe = new CreateStaffRecipe(names);
    return DatastoreUtils.create(recipe);
  }

  public static List<Key> put(final Collection<StaffInfoBean> beans) {
    final Collection<Entity> entities =
        CollectionUtils.collect(beans, StaffEntityInfo.beanToEntity());
    return DatastoreUtils.put(entities);
  }

  public static Collection<StaffInfoBean> query(final Collection<String> names) {
    final List<Entity> entities = DatastoreUtils.query(new StaffNameMultiFilter(names));
    return CollectionUtils.collect(entities, StaffEntityInfo.entityToBean());
  }
}
