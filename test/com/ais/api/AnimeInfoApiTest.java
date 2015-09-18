package com.ais.api;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ais.datastore.AnimeDatastore;
import com.ais.datastore.AnimeEntityInfo;
import com.ais.datastore.PeriodEntityInfo;
import com.ais.external.CoursObject;
import com.ais.external.ExternalAnimeInfoUtils;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class AnimeInfoApiTest {
  // private static final Logger logger = Logger.getLogger(AnimeInfoApiTest.class.getName());
  private static final AnimeInfoApi api = new AnimeInfoApi();
  private static final AnimeEntityInfo animeEntityInfo = new AnimeEntityInfo();

  private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private static DatastoreService datastore;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  @Test
  public void testDeleteAllAnimeInfo() {
    try {
      final BooleanResponse connectResponse = api.connectExternalAndPutCurrent();
      assertTrue(connectResponse.getValue());
      assertTrue(
          CollectionUtils.isNotEmpty(api.getAnimeInfoBeans(new GetAnimeInfoRequest()).getItems()));
      final BooleanResponse deleteResponse = api.deleteAllAnimeInfo();
      assertTrue(deleteResponse.getValue());
      assertTrue(
          CollectionUtils.isEmpty(api.getAnimeInfoBeans(new GetAnimeInfoRequest()).getItems()));
    } catch (final InternalServerErrorException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testDeleteAnimeInfo() {
    try {
      api.connectExternalAndPutCurrent();
      final GetAnimeInfoRequest request = new GetAnimeInfoRequest();
      Collection<AnimeInfoBean> beans = api.getAnimeInfoBeans(request).getItems();
      final AnimeInfoBean bean = beans.iterator().next();
      final IdRequest idRequest = new IdRequest();
      idRequest.setIds(Arrays.asList(bean.getId()));
      api.deleteAnimeInfo(idRequest);
      beans = api.getAnimeInfoBeans(request).getItems();
      assertFalse(CollectionUtils.exists(beans, new Predicate<AnimeInfoBean>() {
        @Override
        public boolean evaluate(final AnimeInfoBean arg0) {
          return arg0.equals(bean);
        }
      }));
    } catch (final InternalServerErrorException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testDeleteCoursObject() {
    try {
      final BooleanResponse connectResponse = api.connectExternalAndPutCurrent();
      assertTrue(connectResponse.getValue());
      assertTrue(CollectionUtils.isNotEmpty(api.getPeriodBeans()));
      final BooleanResponse deleteResponse = api.deleteAllPeriod();
      assertTrue(deleteResponse.getValue());
      assertTrue(CollectionUtils.isEmpty(api.getPeriodBeans()));
    } catch (final InternalServerErrorException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testStoreFromExternalAnimeInfo() {
    try {
      api.connectExternalAndPutAll();
    } catch (final InternalServerErrorException e) {
      fail(e.getMessage());
    }
    final PreparedQuery pQuery = AnimeDatastore.query();
    assertTrue(pQuery.countEntities(FetchOptions.Builder.withDefaults()) > 0);
    for (final Entity entity : pQuery.asIterable()) {
      final Transformer<Entity, AnimeInfoBean> transformer =
          animeEntityInfo.getEntityToAnimeInfoBeanTransformer();
      final AnimeInfoBean bean = transformer.transform(entity);
      try {
        final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, bean.getPeriodId());
        final Entity periodEntity = datastore.get(key);
        assertThat(periodEntity.getProperties().keySet(), not(empty()));
      } catch (final EntityNotFoundException e) {
        fail(e.getMessage());
      }
      assertThat(bean.getId(), is(entity.getKey().getId()));
      assertTrue(StringUtils.isNotEmpty(bean.getTitle()));
      assertTrue(StringUtils.isNotEmpty(bean.getTwitterAccount()));
      for (final String hashTag : bean.getTwitterHashTags()) {
        assertTrue(StringUtils.isNotEmpty(hashTag));
      }
      for (final String shortTitle : bean.getShortTitles()) {
        assertTrue(StringUtils.isNotEmpty(shortTitle));
      }
    }
  }

  @Test
  public void testStoreCurrentAnimeInfo() {
    try {
      api.connectExternalAndPutCurrent();
      final Map<String, CoursObject> map = ExternalAnimeInfoUtils.requestCoursObjectMap();
      final CoursObject current = Collections.max(map.values(), new Comparator<CoursObject>() {
        @Override
        public int compare(final CoursObject o1, final CoursObject o2) {
          return (int) (o1.getId() - o2.getId());
        }
      });
      PreparedQuery pQuery = AnimeDatastore.query();
      final int count = pQuery.countEntities(FetchOptions.Builder.withDefaults());
      assertThat(count, not(0));
      final Transformer<Entity, AnimeInfoBean> transformer =
          animeEntityInfo.getEntityToAnimeInfoBeanTransformer();
      for (final Entity entity : pQuery.asIterable()) {
        final AnimeInfoBean bean = transformer.transform(entity);
        try {
          final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, bean.getPeriodId());
          final Entity periodEntity = datastore.get(key);
          assertThat(periodEntity.getProperties().keySet(), not(empty()));
          final long year = (long) periodEntity.getProperty(PeriodEntityInfo.YEAR_PROPERTY_NAME);
          final long season =
              (long) periodEntity.getProperty(PeriodEntityInfo.SEASON_PROPERTY_NAME);
          assertThat(year, is(current.getYear()));
          assertThat(season, is(current.getCours()));
        } catch (final EntityNotFoundException e) {
          fail(e.getMessage());
        }
        assertTrue(StringUtils.isNotEmpty(bean.getTitle()));
        assertTrue(StringUtils.isNotEmpty(bean.getTwitterAccount()));
        for (final String hashTag : bean.getTwitterHashTags()) {
          assertTrue(StringUtils.isNotEmpty(hashTag));
        }
        for (final String shortTitle : bean.getShortTitles()) {
          assertTrue(StringUtils.isNotEmpty(shortTitle));
        }
      }
      api.connectExternalAndPutCurrent();
      pQuery = AnimeDatastore.query();
      assertThat(pQuery.countEntities(FetchOptions.Builder.withDefaults()), is(count));
      api.deleteAllAnimeInfo();
      api.connectExternalAndPutCurrent();
      pQuery = AnimeDatastore.query();
      assertThat(pQuery.countEntities(FetchOptions.Builder.withDefaults()), is(count));
    } catch (final InternalServerErrorException | IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetAllAnimeInfoBeans() {
    try {
      api.connectExternalAndPutCurrent();
    } catch (final InternalServerErrorException e) {
      fail(e.getMessage());
    }
    final Collection<AnimeInfoBean> beans =
        api.getAnimeInfoBeans(new GetAnimeInfoRequest()).getItems();
    assertTrue(CollectionUtils.isNotEmpty(beans));
    for (final AnimeInfoBean bean : beans) {
      assertTrue(StringUtils.isNotEmpty(bean.getTitle()));
      assertTrue(StringUtils.isNotEmpty(bean.getTwitterAccount()));
      for (final String hashTag : bean.getTwitterHashTags()) {
        assertTrue(StringUtils.isNotEmpty(hashTag));
      }
      try {
        final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, bean.getPeriodId());
        final Entity periodEntity = datastore.get(key);
        assertThat(periodEntity.getProperties().keySet(), not(empty()));
        final long year = (long) periodEntity.getProperty(PeriodEntityInfo.YEAR_PROPERTY_NAME);
        final long season = (long) periodEntity.getProperty(PeriodEntityInfo.SEASON_PROPERTY_NAME);
        assertThat(year, allOf(greaterThan(2000L), lessThan(3000L)));
        assertThat(season, allOf(greaterThan(0L), lessThan(5L)));
      } catch (final EntityNotFoundException e) {
        fail(e.getMessage());
      }
      for (final String shortTitle : bean.getShortTitles()) {
        assertTrue(StringUtils.isNotEmpty(shortTitle));
      }
    }
  }

  @Test
  public void testGetAnimeInfoBeans() {
    try {
      api.connectExternalAndPutAll();
      final Map<String, CoursObject> coursMap = ExternalAnimeInfoUtils.requestCoursObjectMap();
      for (final CoursObject coursObject : coursMap.values()) {
        final GetAnimeInfoRequest request = new GetAnimeInfoRequest();
        final PeriodBean requestPeriod = new PeriodBean();
        requestPeriod.setYear(coursObject.getYear());
        requestPeriod.setSeason(coursObject.getCours());
        request.setPeriod(requestPeriod);
        final Collection<AnimeInfoBean> animeInfoBeans = api.getAnimeInfoBeans(request).getItems();
        assertTrue(CollectionUtils.isNotEmpty(animeInfoBeans));
        for (final AnimeInfoBean bean : animeInfoBeans) {
          assertTrue(StringUtils.isNotEmpty(bean.getTitle()));
          assertTrue(StringUtils.isNotEmpty(bean.getTwitterAccount()));
          for (final String hashTag : bean.getTwitterHashTags()) {
            assertTrue(StringUtils.isNotEmpty(hashTag));
          }
          try {
            final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, bean.getPeriodId());
            final Entity periodEntity = datastore.get(key);
            assertThat(periodEntity.getProperties().keySet(), not(empty()));
            final long year = (long) periodEntity.getProperty(PeriodEntityInfo.YEAR_PROPERTY_NAME);
            final long season =
                (long) periodEntity.getProperty(PeriodEntityInfo.SEASON_PROPERTY_NAME);
            assertThat(year, is(coursObject.getYear()));
            assertThat(season, is(coursObject.getCours()));
          } catch (final EntityNotFoundException e) {
            fail(e.getMessage());
          }
          for (final String shortTitle : bean.getShortTitles()) {
            assertTrue(StringUtils.isNotEmpty(shortTitle));
          }
        }
      }
    } catch (final InternalServerErrorException | IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetAnimeInfoBeansForYear() {
    try {
      api.connectExternalAndPutAll();
      final Map<String, CoursObject> coursMap = ExternalAnimeInfoUtils.requestCoursObjectMap();
      final Collection<Long> years =
          CollectionUtils.collect(coursMap.values(), new Transformer<CoursObject, Long>() {
            @Override
            public Long transform(final CoursObject arg0) {
              return arg0.getYear();
            }
          });
      final Set<Long> yearSet = new HashSet<Long>(years);
      assertTrue(CollectionUtils.isNotEmpty(yearSet));
      for (final Long year : yearSet) {
        final GetAnimeInfoRequest request = new GetAnimeInfoRequest();
        final PeriodBean requestPeriod = new PeriodBean();
        requestPeriod.setYear(year);
        request.setPeriod(requestPeriod);
        final Collection<AnimeInfoBean> animeInfoBeans = api.getAnimeInfoBeans(request).getItems();
        assertTrue(CollectionUtils.isNotEmpty(animeInfoBeans));
        for (final AnimeInfoBean bean : animeInfoBeans) {
          assertTrue(StringUtils.isNotEmpty(bean.getTitle()));
          assertTrue(StringUtils.isNotEmpty(bean.getTwitterAccount()));
          for (final String hashTag : bean.getTwitterHashTags()) {
            assertTrue(StringUtils.isNotEmpty(hashTag));
          }
          try {
            final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, bean.getPeriodId());
            final Entity periodEntity = datastore.get(key);
            assertThat(periodEntity.getProperties().keySet(), not(empty()));
            final long storedYear =
                (long) periodEntity.getProperty(PeriodEntityInfo.YEAR_PROPERTY_NAME);
            final long season =
                (long) periodEntity.getProperty(PeriodEntityInfo.SEASON_PROPERTY_NAME);
            assertThat(storedYear, is(year));
            assertThat(season, allOf(greaterThan(0L), lessThan(5L)));
          } catch (final EntityNotFoundException e) {
            fail(e.getMessage());
          }
          for (final String shortTitle : bean.getShortTitles()) {
            assertTrue(StringUtils.isNotEmpty(shortTitle));
          }
        }
      }
    } catch (final InternalServerErrorException | IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetAnimeInfoBeansWithId() {
    try {
      api.connectExternalAndPutAll();
      final Collection<PeriodBean> periodBeans = api.getPeriodBeans();
      assertThat(periodBeans, not(empty()));
      for (final PeriodBean period : periodBeans) {
        final GetAnimeInfoRequest request = new GetAnimeInfoRequest();
        final PeriodBean requestPeriod = new PeriodBean();
        requestPeriod.setId(period.getId());
        request.setPeriod(requestPeriod);
        final Collection<AnimeInfoBean> animeInfoBeans = api.getAnimeInfoBeans(request).getItems();
        assertTrue(CollectionUtils.isNotEmpty(animeInfoBeans));
        for (final AnimeInfoBean bean : animeInfoBeans) {
          assertTrue(StringUtils.isNotEmpty(bean.getTitle()));
          assertTrue(StringUtils.isNotEmpty(bean.getTwitterAccount()));
          for (final String hashTag : bean.getTwitterHashTags()) {
            assertTrue(StringUtils.isNotEmpty(hashTag));
          }
          try {
            final Key key = KeyFactory.createKey(PeriodEntityInfo.KIND_NAME, bean.getPeriodId());
            final Entity periodEntity = datastore.get(key);
            assertThat(periodEntity.getProperties().keySet(), not(empty()));
            final long year = (long) periodEntity.getProperty(PeriodEntityInfo.YEAR_PROPERTY_NAME);
            final long season =
                (long) periodEntity.getProperty(PeriodEntityInfo.SEASON_PROPERTY_NAME);
            assertThat(year, is(period.getYear()));
            assertThat(season, is(period.getSeason()));
          } catch (final EntityNotFoundException e) {
            fail(e.getMessage());
          }
          for (final String shortTitle : bean.getShortTitles()) {
            assertTrue(StringUtils.isNotEmpty(shortTitle));
          }
        }
      }
    } catch (final InternalServerErrorException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetCoursObjects() {
    try {
      api.connectExternalAndPutAll();
      final Collection<PeriodBean> beans = api.getPeriodBeans();
      assertTrue(CollectionUtils.isNotEmpty(beans));
      for (final PeriodBean bean : beans) {
        assertTrue(0 < bean.getId());
        assertTrue(2000 < bean.getYear() && bean.getYear() < 3000);
        assertTrue(0 < bean.getSeason() && bean.getSeason() < 5);
      }
    } catch (final InternalServerErrorException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testPutAnimeInfoBeans() {
    try {
      api.connectExternalAndPutCurrent();
      final AnimeInfoBean animeBean = new AnimeInfoBean();
      animeBean.setTitle("test title");
      final Collection<PeriodBean> periodBeans = api.getPeriodBeans();
      final PeriodBean current = periodBeans.iterator().next();
      animeBean.setPeriodId(current.getId());
      final PostAnimeInfoRequest request = new PostAnimeInfoRequest();
      request.setItems(Arrays.asList(animeBean));
      final CollectionResponse<AnimeInfoBean> response = api.putAnimeInfoBeans(request);
      Collection<AnimeInfoBean> beans = response.getItems();
      final AnimeInfoBean putBean = CollectionUtils.find(beans, new Predicate<AnimeInfoBean>() {
        @Override
        public boolean evaluate(final AnimeInfoBean arg0) {
          return StringUtils.equals(arg0.getTitle(), animeBean.getTitle());
        }
      });
      assertThat(putBean.getTitle(), is(animeBean.getTitle()));
      assertThat(putBean.getPeriodId(), is(animeBean.getPeriodId()));

      putBean.setTitle("test title2");
      request.setItems(Arrays.asList(putBean));
      beans = api.putAnimeInfoBeans(request).getItems();
      assertTrue(CollectionUtils.isNotEmpty(beans));
      final GetAnimeInfoRequest getRequest = new GetAnimeInfoRequest();
      getRequest.setPeriod(current);
      beans = api.getAnimeInfoBeans(getRequest).getItems();
      assertTrue(CollectionUtils.exists(beans, new Predicate<AnimeInfoBean>() {
        @Override
        public boolean evaluate(final AnimeInfoBean arg0) {
          return StringUtils.equals(arg0.getTitle(), putBean.getTitle());
        }
      }));
      assertFalse(CollectionUtils.exists(beans, new Predicate<AnimeInfoBean>() {
        @Override
        public boolean evaluate(final AnimeInfoBean arg0) {
          return StringUtils.equals(arg0.getTitle(), animeBean.getTitle());
        }
      }));
    } catch (final InternalServerErrorException e) {
      fail(e.getMessage());
    }

  }

  @Test
  public void testPutAnimeInfoBeansForFailed() {
    try {
      api.connectExternalAndPutCurrent();
      final PostAnimeInfoRequest request = new PostAnimeInfoRequest();
      request.setItems(null);
      CollectionResponse<AnimeInfoBean> response = api.putAnimeInfoBeans(request);
      assertThat(response, not(nullValue()));
      Collection<AnimeInfoBean> items = response.getItems();
      assertThat(items, not(nullValue()));
      assertThat(items.size(), is(0));

      final PeriodBean period = api.getPeriodBeans().iterator().next();
      AnimeInfoBean item = new AnimeInfoBean();
      item.setPeriodId(period.getId());
      request.setItems(Arrays.asList(item));
      response = api.putAnimeInfoBeans(request);
      assertThat(response, not(nullValue()));
      items = response.getItems();
      assertThat(items, not(nullValue()));
      assertThat(items.size(), is(1));
      for (final AnimeInfoBean animeInfoBean : items) {
        assertThat(animeInfoBean.getId(), not(0L));
      }

      try {
        api.putAnimeInfoBeans(null);
        fail();
      } catch (final NullPointerException e) {
      }
      try {
        item = new AnimeInfoBean();
        request.setItems(Arrays.asList(item));
        api.putAnimeInfoBeans(request);
        fail();
      } catch (final IllegalArgumentException e) {
      }

    } catch (final InternalServerErrorException e) {
      fail(e.getMessage());
    }
  }
}
