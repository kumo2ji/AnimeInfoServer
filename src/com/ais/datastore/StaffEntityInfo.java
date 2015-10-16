package com.ais.datastore;

import org.apache.commons.collections4.Transformer;

import com.ais.api.StaffInfoBean;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;

public class StaffEntityInfo {
  public static final String KIND_NAME = "StaffInfo";
  public static final String NAME_PROPERTY_NAME = "name";

  public static Transformer<StaffInfoBean, Entity> beanToEntity() {
    return new Transformer<StaffInfoBean, Entity>() {
      @Override
      public Entity transform(final StaffInfoBean arg0) {
        final Entity entity = new Entity(KeyFactory.createKey(KIND_NAME, arg0.getId()));
        entity.setProperty(NAME_PROPERTY_NAME, arg0.getName());
        return entity;
      }
    };
  }

  public static Transformer<Entity, StaffInfoBean> entityToBean() {
    return new Transformer<Entity, StaffInfoBean>() {
      @Override
      public StaffInfoBean transform(final Entity arg0) {
        final StaffInfoBean bean = new StaffInfoBean();
        bean.setId(arg0.getKey().getId());
        bean.setName((String) arg0.getProperty(NAME_PROPERTY_NAME));
        return bean;
      }
    };
  }
}
