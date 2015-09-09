package com.ais.external;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import com.ais.api.CoursObject;


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

  public static Collection<AnimeBaseObject> requestAnimeBaseObjects(final String year,
      final String cours) throws IOException {
    return requestAnimeBaseObjectsForUrl(server.getBaseObjectUrlString(year, cours));
  }

  private static Collection<AnimeBaseObject> requestAnimeBaseObjectsForUrl(final String urlString)
      throws IOException {
    return ExternalServerUtils.requestForCollection(urlString, server.getAnimeBaseObjectsFunc());
  }
}
