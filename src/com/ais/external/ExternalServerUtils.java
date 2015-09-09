package com.ais.external;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections4.Transformer;

import com.ais.utils.Utils;

public class ExternalServerUtils {
  private ExternalServerUtils() {}

  public static InputStream openStream(final String urlString) throws IOException {
    final URL url = new URL(urlString);
    return url.openStream();
  }

  public static <T> Map<String, T> requestForMap(final String urlString,
      final Transformer<Reader, Map<String, T>> convertFunc) throws IOException {
    final InputStream inputStream = openStream(urlString);
    final InputStreamReader reader = Utils.toInputStreamReader(inputStream);
    return convertFunc.transform(reader);
  }

  public static <T> Collection<T> requestForCollection(final String urlString,
      final Transformer<Reader, Collection<T>> convertFunc) throws IOException {
    final InputStream inputStream = openStream(urlString);
    final InputStreamReader reader = Utils.toInputStreamReader(inputStream);
    return convertFunc.transform(reader);
  }
}
