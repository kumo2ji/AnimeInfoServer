package com.ais.external;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;


public class ExternalAnimeInfoUtils {
  private static final ExternalAnimeInfo server = new ExternalAnimeInfo();

  private ExternalAnimeInfoUtils() {}

  public static InputStream openAnimeCoursObjectStream() throws IOException {
    return ExternalServerUtils.openStream(server.getCoursObjectUrlString());
  }

  public static InputStream openAnimeBaseObjectStream(final String year) throws IOException {
    return ExternalServerUtils.openStream(server.getBaseObjectUrlString(year));
  }

  public static Map<String, CoursObject> requestCoursObjectMap() throws IOException {
    return ExternalServerUtils.requestForMap(server.getCoursObjectUrlString(),
        server.getCoursObjectMapFunc());
  }

  public static Collection<AnimeBaseObject> requestAnimeBaseObjects(final String year)
      throws IOException {
    return requestAnimeBaseObjectsForUrl(server.getBaseObjectUrlString(year));
  }

  public static Collection<AnimeBaseObject> requestAnimeBaseObjects(final long year,
      final long cours) throws IOException {
    return CollectionUtils.collect(
        requestAnimeBaseObjectsForUrl(
            server.getBaseObjectUrlString(String.valueOf(year), String.valueOf(cours))),
        new Transformer<AnimeBaseObject, AnimeBaseObject>() {
          @Override
          public AnimeBaseObject transform(final AnimeBaseObject arg0) {
            final CoursObject coursObject = new CoursObject();
            coursObject.setId(arg0.getCours_id());
            coursObject.setYear(year);
            coursObject.setCours(cours);
            arg0.setCoursObject(coursObject);
            return arg0;
          }
        });
  }

  private static Collection<AnimeBaseObject> requestAnimeBaseObjectsForUrl(final String urlString)
      throws IOException {
    return ExternalServerUtils.requestForCollection(urlString, server.getAnimeBaseObjectsFunc());
  }
}
