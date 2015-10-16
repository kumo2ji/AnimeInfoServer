package com.ais.datastore;

import java.util.Collection;

import org.apache.commons.collections4.Transformer;

import com.ais.api.StaffInfoBean;
import com.google.appengine.api.datastore.Entity;

public class CreateStaffRecipe implements Creatable<Long, StaffInfoBean> {
  private final Collection<Long> targets;

  public CreateStaffRecipe(final Collection<Long> targets) {
    super();
    this.targets = targets;
  }

  @Override
  public Collection<Long> getTargets() {
    return targets;
  }

  @Override
  public Transformer<Entity, StaffInfoBean> toBeanTransformer() {
    return StaffEntityInfo.entityToBean();
  }

  @Override
  public Boolean equals(final Long t, final StaffInfoBean k) {
    return t == k.getAnimeId();
  }

  @Override
  public Transformer<Long, Entity> toEntityTransformer() {
    return new Transformer<Long, Entity>() {
      @Override
      public Entity transform(final Long arg0) {
        final Entity entity = new Entity(StaffEntityInfo.KIND_NAME);
        entity.setProperty(StaffEntityInfo.ANIME_ID_PROPERTY_NAME, arg0);
        return entity;
      }
    };
  }

  @Override
  public MultiFilterable<Long> getMultiFilter() {
    return new StaffAnimeIdMultiFilter(targets);
  }
}
