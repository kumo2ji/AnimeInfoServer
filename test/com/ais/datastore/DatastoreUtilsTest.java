package com.ais.datastore;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ais.api.CoursObject;
import com.ais.external.AnimeBaseObject;
import com.ais.external.ExternalAnimeInfoUtils;
import com.ais.utils.BiFunc;
import com.ais.utils.Utils;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class DatastoreUtilsTest {
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
  public final void testPutAnimeBaseObjects() {
    try {
      final Collection<AnimeBaseObject> baseList =
          ExternalAnimeInfoUtils.requestAnimeBaseObjects("2014", "2");
      final Map<String, CoursObject> coursMap = ExternalAnimeInfoUtils.requestCoursObjectMap();
      final List<Key> keyList = DatastoreUtils.putAnimeBaseObjects(baseList, coursMap);
      assertTrue(!keyList.isEmpty());
      final PreparedQuery preparedQuery = DatastoreUtils.queryAnimeInfoBeans();
      assertThat(baseList.size(),
          is(preparedQuery.countEntities(FetchOptions.Builder.withDefaults())));
      assertTrue(Utils.equals(preparedQuery.asIterable(), baseList,
          new BiFunc<Entity, AnimeBaseObject, Boolean>() {
            @Override
            public Boolean apply(final Entity arg1, final AnimeBaseObject arg2) {
              return StringUtils.equals((String) arg1.getProperty("title"), arg2.getTitle());
            }
          }));
    } catch (final IOException e) {
      fail(e.getMessage());
    }
  }
}
