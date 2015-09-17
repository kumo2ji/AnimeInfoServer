package com.ais.datastore;

import org.apache.commons.collections4.Transformer;

import com.ais.api.PeriodBean;
import com.ais.external.CoursObject;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class PeriodEntityInfo {
  public static final String KIND_NAME = "Period";
  public static final String YEAR_PROPERTY_NAME = "year";
  public static final String SEASON_PROPERTY_NAME = "season";

  public Transformer<CoursObject, Entity> getCoursObjectToEntityTransformer() {
    return new Transformer<CoursObject, Entity>() {
      @Override
      public Entity transform(final CoursObject arg0) {
        final Entity entity = new Entity(KIND_NAME);
        entity.setProperty(YEAR_PROPERTY_NAME, arg0.getYear());
        entity.setProperty(SEASON_PROPERTY_NAME, arg0.getCours());
        return entity;
      }
    };
  }

  public Transformer<Entity, PeriodBean> getEntityToPeriodBeanTransformer() {
    return new Transformer<Entity, PeriodBean>() {
      @Override
      public PeriodBean transform(final Entity arg0) {
        final PeriodBean period = new PeriodBean();
        period.setId(arg0.getKey().getId());
        period.setYear((long) arg0.getProperty(YEAR_PROPERTY_NAME));
        period.setSeason((long) arg0.getProperty(SEASON_PROPERTY_NAME));
        return period;
      }
    };
  }

  public Transformer<Long, PeriodBean> getIdToPeriodBeanTransformer() {
    return new Transformer<Long, PeriodBean>() {
      @Override
      public PeriodBean transform(final Long arg0) {
        final Key key = KeyFactory.createKey(KIND_NAME, arg0);
        try {
          final Entity entity = DatastoreUtils.getEntity(key);
          return getEntityToPeriodBeanTransformer().transform(entity);
        } catch (final EntityNotFoundException e) {
          e.printStackTrace();
        }
        return null;
      }
    };
  }
}
