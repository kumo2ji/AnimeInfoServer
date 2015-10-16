package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

import com.ais.api.StaffInfoBean;
import com.google.appengine.api.datastore.Entity;

public class CreateStaffRecipe implements Creatable<String, StaffInfoBean> {
  private final Collection<String> targets;

  public CreateStaffRecipe(final Collection<String> targets) {
    super();
    this.targets = targets;
  }

  @Override
  public Collection<String> getTargets() {
    return targets;
  }

  @Override
  public Transformer<Entity, StaffInfoBean> toBeanTransformer() {
    return new Transformer<Entity, StaffInfoBean>() {
      @Override
      public StaffInfoBean transform(final Entity arg0) {
        final StaffInfoBean bean = new StaffInfoBean();
        final String name = (String) arg0.getProperty(StaffEntityInfo.NAME_PROPERTY_NAME);
        bean.setName(name);
        return bean;
      }
    };
  }

  @Override
  public Boolean equals(final String t, final StaffInfoBean k) {
    return StringUtils.equalsIgnoreCase(t, k.getName());
  }

  @Override
  public Transformer<String, Entity> toEntityTransformer() {
    return new Transformer<String, Entity>() {
      @Override
      public Entity transform(final String arg0) {
        final Entity entity = new Entity(StaffEntityInfo.KIND_NAME);
        entity.setProperty(StaffEntityInfo.NAME_PROPERTY_NAME, arg0);
        return entity;
      }
    };
  }

  @Override
  public MultiFilterable<String> getMultiFilter() {
    return new StaffNameMultiFilter(targets);
  }
}
