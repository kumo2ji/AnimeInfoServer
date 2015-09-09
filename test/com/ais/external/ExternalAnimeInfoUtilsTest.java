package com.ais.external;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.ais.api.CoursObject;
import com.ais.utils.Utils;


public class ExternalAnimeInfoUtilsTest {
  private static Logger logger = Logger.getLogger(ExternalAnimeInfoUtilsTest.class.getName());

  @Test
  public void testOpenAnimeObject() {
    try {
      final InputStream inputStream = ExternalAnimeInfoUtils.openAnimeCoursObjectStream();
      final String responseString = Utils.toString(inputStream);
      assertTrue(StringUtils.isNotEmpty(responseString));
      logger.info(responseString);

      final InputStream baseObjectStream = ExternalAnimeInfoUtils.openAnimeBaseObjectStream("2014");
      final String baseObjectString = Utils.toString(baseObjectStream);
      assertTrue(!baseObjectString.isEmpty());
    } catch (final IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testRequestCoursObject() {
    try {
      final Map<String, CoursObject> map = ExternalAnimeInfoUtils.requestCoursObjectMap();
      assertTrue(!map.isEmpty());
      for (final Entry<String, CoursObject> entry : map.entrySet()) {
        final String idString = entry.getKey();
        final int parsedId = Integer.parseInt(idString);
        final CoursObject coursObject = entry.getValue();
        final long id = coursObject.getId();
        final long year = coursObject.getYear();
        final long cours = coursObject.getCours();
        assertTrue(parsedId == id);
        assertTrue(id > 0);
        assertTrue(year > 2000);
        assertTrue(0 < cours);
        assertTrue(cours < 5);
      }
    } catch (final IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testRequestAnimeBaseObject() {
    try {
      final Collection<AnimeBaseObject> list =
          ExternalAnimeInfoUtils.requestAnimeBaseObjects("2014");
      assertTrue(!list.isEmpty());
      for (final AnimeBaseObject animeBaseObject : list) {
        final long id = animeBaseObject.getId();
        final String title = animeBaseObject.getTitle();
        assertTrue(id > 0);
        assertTrue(!title.isEmpty());
      }
    } catch (final IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testRequestAnimeBaseObjectForCours() {
    try {
      final Collection<AnimeBaseObject> list =
          ExternalAnimeInfoUtils.requestAnimeBaseObjects("2014", "1");
      assertTrue(!list.isEmpty());
      for (final AnimeBaseObject animeBaseObject : list) {
        assertTrue(animeBaseObject.getId() > 0);
        assertTrue(!animeBaseObject.getTitle().isEmpty());
        // assertTrue(!animeBaseObject.getPublic_url().isEmpty());
        assertTrue(!animeBaseObject.getTwitter_account().isEmpty());
        assertTrue(!animeBaseObject.getTwitter_hash_tag().isEmpty());
        assertTrue(animeBaseObject.getCours_id() > 0);
        final String createdAtString = animeBaseObject.getCreated_at();
        final String updatedAtString = animeBaseObject.getUpdated_at();
        assertTrue(!createdAtString.isEmpty());
        assertTrue(!updatedAtString.isEmpty());
      }
    } catch (final IOException e) {
      fail(e.getMessage());
    }
  }
}
