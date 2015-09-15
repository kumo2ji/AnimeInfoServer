package com.ais.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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

import com.ais.datastore.AnimeEntityInfo;
import com.ais.datastore.DatastoreUtils;
import com.ais.external.ExternalAnimeInfoUtils;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class AnimeInfoApiTest {
  // private static final Logger logger = Logger.getLogger(AnimeInfoApiTest.class.getName());
  private static final AnimeInfoApi api = new AnimeInfoApi();
  private static final AnimeEntityInfo animeEntityInfo = new AnimeEntityInfo();

  private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() throws Exception {
    helper.setUp();
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
      assertTrue(CollectionUtils.isNotEmpty(api.getCoursObjects()));
      final BooleanResponse deleteResponse = api.deleteCoursObject();
      assertTrue(deleteResponse.getValue());
      assertTrue(CollectionUtils.isEmpty(api.getCoursObjects()));
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
    final PreparedQuery pQuery = DatastoreUtils.queryAnimeInfoBeans();
    assertTrue(pQuery.countEntities(FetchOptions.Builder.withDefaults()) > 0);
    for (final Entity entity : pQuery.asIterable()) {
      final Transformer<Entity, AnimeInfoBean> transformer =
          animeEntityInfo.getEntityToAnimeInfoBeanTransformer();
      final AnimeInfoBean bean = transformer.transform(entity);
      final CoursObject coursObject = bean.getCoursObject();
      assertTrue(2000 < coursObject.getYear() && coursObject.getYear() < 3000);
      assertTrue(0 < coursObject.getCours() && coursObject.getCours() < 5);
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
      PreparedQuery pQuery = DatastoreUtils.queryAnimeInfoBeans();
      final int count = pQuery.countEntities(FetchOptions.Builder.withDefaults());
      assertThat(count, not(0));
      final Transformer<Entity, AnimeInfoBean> transformer =
          animeEntityInfo.getEntityToAnimeInfoBeanTransformer();
      for (final Entity entity : pQuery.asIterable()) {
        final AnimeInfoBean bean = transformer.transform(entity);
        final CoursObject coursObject = bean.getCoursObject();
        assertThat(coursObject, is(current));
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
      pQuery = DatastoreUtils.queryAnimeInfoBeans();
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
      final CoursObject coursObject = bean.getCoursObject();
      assertTrue(2000 < coursObject.getYear() && coursObject.getYear() < 3000);
      assertTrue(0 < coursObject.getCours() && coursObject.getCours() < 5);
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
        final CoursObject requestCoursObject = new CoursObject();
        requestCoursObject.setYear(coursObject.getYear());
        requestCoursObject.setCours(coursObject.getCours());
        request.setCoursObject(requestCoursObject);
        final Collection<AnimeInfoBean> animeInfoBeans = api.getAnimeInfoBeans(request).getItems();
        assertTrue(CollectionUtils.isNotEmpty(animeInfoBeans));
        for (final AnimeInfoBean bean : animeInfoBeans) {
          assertTrue(StringUtils.isNotEmpty(bean.getTitle()));
          assertTrue(StringUtils.isNotEmpty(bean.getTwitterAccount()));
          for (final String hashTag : bean.getTwitterHashTags()) {
            assertTrue(StringUtils.isNotEmpty(hashTag));
          }
          final CoursObject storedCoursObject = bean.getCoursObject();
          assertThat(storedCoursObject, is(coursObject));
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
        final CoursObject requestCoursObject = new CoursObject();
        requestCoursObject.setYear(year);
        request.setCoursObject(requestCoursObject);
        final Collection<AnimeInfoBean> animeInfoBeans = api.getAnimeInfoBeans(request).getItems();
        assertTrue(CollectionUtils.isNotEmpty(animeInfoBeans));
        for (final AnimeInfoBean bean : animeInfoBeans) {
          assertTrue(StringUtils.isNotEmpty(bean.getTitle()));
          assertTrue(StringUtils.isNotEmpty(bean.getTwitterAccount()));
          for (final String hashTag : bean.getTwitterHashTags()) {
            assertTrue(StringUtils.isNotEmpty(hashTag));
          }
          final CoursObject storedCoursObject = bean.getCoursObject();
          assertThat(storedCoursObject.getYear(), is(year));
          assertTrue(0 < storedCoursObject.getCours() && storedCoursObject.getCours() < 5);
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
      final Map<String, CoursObject> coursMap = ExternalAnimeInfoUtils.requestCoursObjectMap();
      for (final CoursObject coursObject : coursMap.values()) {
        final GetAnimeInfoRequest request = new GetAnimeInfoRequest();
        final CoursObject requestCoursObject = new CoursObject();
        requestCoursObject.setId(coursObject.getId());
        request.setCoursObject(requestCoursObject);
        final Collection<AnimeInfoBean> animeInfoBeans = api.getAnimeInfoBeans(request).getItems();
        assertTrue(CollectionUtils.isNotEmpty(animeInfoBeans));
        for (final AnimeInfoBean bean : animeInfoBeans) {
          assertTrue(StringUtils.isNotEmpty(bean.getTitle()));
          assertTrue(StringUtils.isNotEmpty(bean.getTwitterAccount()));
          for (final String hashTag : bean.getTwitterHashTags()) {
            assertTrue(StringUtils.isNotEmpty(hashTag));
          }
          final CoursObject storedCoursObject = bean.getCoursObject();
          assertThat(storedCoursObject, is(coursObject));
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
  public void testGetCoursObjects() {
    try {
      api.connectExternalAndPutAll();
      final Collection<CoursObject> coursObjects = api.getCoursObjects();
      assertTrue(CollectionUtils.isNotEmpty(coursObjects));
      for (final CoursObject coursObject : coursObjects) {
        assertTrue(0 < coursObject.getId());
        assertTrue(2000 < coursObject.getYear() && coursObject.getYear() < 3000);
        assertTrue(0 < coursObject.getCours() && coursObject.getCours() < 5);
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
      final Collection<CoursObject> coursObjects = api.getCoursObjects();
      final CoursObject current = coursObjects.iterator().next();
      animeBean.setCoursObject(current);
      final PostAnimeInfoRequest request = new PostAnimeInfoRequest();
      request.setBeans(Arrays.asList(animeBean));
      final CollectionResponse<AnimeInfoBean> response = api.putAnimeInfoBeans(request);
      Collection<AnimeInfoBean> beans = response.getItems();
      final AnimeInfoBean putBean = CollectionUtils.find(beans, new Predicate<AnimeInfoBean>() {
        @Override
        public boolean evaluate(final AnimeInfoBean arg0) {
          return StringUtils.equals(arg0.getTitle(), animeBean.getTitle());
        }
      });
      assertThat(putBean.getTitle(), is(animeBean.getTitle()));
      assertThat(putBean.getCoursObject(), is(animeBean.getCoursObject()));

      putBean.setTitle("test title2");
      request.setBeans(Arrays.asList(putBean));
      beans = api.putAnimeInfoBeans(request).getItems();
      assertTrue(CollectionUtils.isNotEmpty(beans));
      final GetAnimeInfoRequest getRequest = new GetAnimeInfoRequest();
      getRequest.setCoursObject(current);
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
}
