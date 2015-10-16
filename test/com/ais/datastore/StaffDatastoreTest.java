package com.ais.datastore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ais.api.AnimeInfoApi;
import com.ais.api.AnimeInfoBean;
import com.ais.api.GetAnimeInfoRequest;
import com.ais.api.StaffInfoBean;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class StaffDatastoreTest {
  private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private static final AnimeInfoApi api = new AnimeInfoApi();

  @Before
  public void setUp() throws Exception {
    helper.setUp();
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  @Test
  public void testCreate() {
    try {
      final Collection<AnimeInfoBean> beans = getAnimeInfoBeans();
      final Collection<Long> animeIds = getAnimeIds(beans);
      final List<Key> keys = StaffDatastore.create(animeIds);
      assertThat(keys, not(empty()));
      final Map<Key, Entity> map = DatastoreUtils.get(keys);
      for (final Entity entity : map.values()) {
        final long animeId = (long) entity.getProperty(StaffEntityInfo.ANIME_ID_PROPERTY_NAME);
        assertTrue(animeIds.contains(animeId));
      }
      animeIds.add(2000000L);
      final List<Key> keys2 = StaffDatastore.create(animeIds);
      assertThat(keys2.size(), is(1));
      final Entity entity = DatastoreUtils.get(CollectionUtils.get(keys2, 0));
      final long animeId = (long) entity.getProperty(StaffEntityInfo.ANIME_ID_PROPERTY_NAME);
      assertThat(animeId, is(2000000L));
    } catch (final InternalServerErrorException | EntityNotFoundException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testPut() {
    try {
      final Collection<AnimeInfoBean> animeBeans = getAnimeInfoBeans();
      final Collection<Long> animeIds = getAnimeIds(animeBeans);
      final List<Key> animeKeys = StaffDatastore.create(animeIds);
      final Map<Key, Entity> map = DatastoreUtils.get(animeKeys);
      final Collection<StaffInfoBean> staffBeans =
          CollectionUtils.collect(map.values(), new Transformer<Entity, StaffInfoBean>() {
            @Override
            public StaffInfoBean transform(final Entity arg0) {
              final StaffInfoBean staff = StaffEntityInfo.entityToBean().transform(arg0);
              final long id = staff.getId();
              staff.setDirectors(Arrays.asList("director" + id));
              staff.setWriters(Arrays.asList("writer" + id));
              staff.setMusicians(Arrays.asList("musician" + id));
              staff.setStudios(Arrays.asList("studio" + id));
              return staff;
            }
          });
      final List<Key> staffKeys = StaffDatastore.put(staffBeans);
      assertThat(staffBeans, not(empty()));
      final Map<Key, Entity> stored = DatastoreUtils.get(staffKeys);
      for (final Entity entity : stored.values()) {
        final long id = entity.getKey().getId();
        @SuppressWarnings("unchecked")
        final Collection<String> directors =
            (Collection<String>) entity.getProperty(StaffEntityInfo.DIRECTORS_PROPERTY_NAME);
        @SuppressWarnings("unchecked")
        final Collection<String> writers =
            (Collection<String>) entity.getProperty(StaffEntityInfo.WRITERS_PROPERTY_NAME);
        @SuppressWarnings("unchecked")
        final Collection<String> musicians =
            (Collection<String>) entity.getProperty(StaffEntityInfo.MUSICIANS_PROPERTY_NAME);
        @SuppressWarnings("unchecked")
        final Collection<String> studios =
            (Collection<String>) entity.getProperty(StaffEntityInfo.STUDIOS_PROPERTY_NAME);
        assertThat(CollectionUtils.get(directors, 0), is("director" + id));
        assertThat(CollectionUtils.get(writers, 0), is("writer" + id));
        assertThat(CollectionUtils.get(musicians, 0), is("musician" + id));
        assertThat(CollectionUtils.get(studios, 0), is("studio" + id));
      }
    } catch (final InternalServerErrorException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private Collection<AnimeInfoBean> getAnimeInfoBeans() throws InternalServerErrorException {
    api.connectExternalAndPutCurrent();
    final GetAnimeInfoRequest request = new GetAnimeInfoRequest();
    final CollectionResponse<AnimeInfoBean> response = api.getAnimeInfoBeans(request);
    return response.getItems();
  }

  private Collection<Long> getAnimeIds(final Collection<AnimeInfoBean> beans) {
    return CollectionUtils.collect(beans, new Transformer<AnimeInfoBean, Long>() {
      @Override
      public Long transform(final AnimeInfoBean arg0) {
        return arg0.getId();
      }
    });
  }
}
