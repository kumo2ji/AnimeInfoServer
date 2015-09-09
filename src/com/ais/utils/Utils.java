package com.ais.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;


public final class Utils {
  private Utils() {}

  public static String toString(final InputStream inputStream) throws IOException {
    final InputStreamReader inputReader = toInputStreamReader(inputStream);
    final BufferedReader bufferedReader = new BufferedReader(inputReader);
    final StringBuilder builder = new StringBuilder();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      builder.append(line);
    }
    bufferedReader.close();
    return builder.toString();
  }

  public static InputStreamReader toInputStreamReader(final InputStream inputStream) {
    return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
  }

  public static <T1, T2> boolean equals(final Iterable<T1> collection1,
      final Iterable<T2> collection2, final BiFunc<T1, T2, Boolean> equalsFunc) {
    return CollectionUtils.matchesAll(collection1, new Predicate<T1>() {
      @Override
      public boolean evaluate(final T1 t1) {
        return CollectionUtils.exists(collection2, new Predicate<T2>() {
          @Override
          public boolean evaluate(final T2 t2) {
            return equalsFunc.apply(t1, t2);
          }
        });
      }
    }) && CollectionUtils.matchesAll(collection2, new Predicate<T2>() {
      @Override
      public boolean evaluate(final T2 t2) {
        return CollectionUtils.exists(collection1, new Predicate<T1>() {
          @Override
          public boolean evaluate(final T1 t1) {
            return equalsFunc.apply(t1, t2);
          }
        });
      }
    });
  }

  public static <T1, T2> boolean contains(final Iterable<T1> collection1,
      final Iterable<T2> collection2, final BiFunc<T1, T2, Boolean> equalsFunc) {
    return CollectionUtils.matchesAll(collection1, new Predicate<T1>() {
      @Override
      public boolean evaluate(final T1 t1) {
        return CollectionUtils.exists(collection2, new Predicate<T2>() {
          @Override
          public boolean evaluate(final T2 t2) {
            return equalsFunc.apply(t1, t2);
          }
        });
      }
    });
  }

  public static <T> boolean contains(final Iterable<T> collection, final Predicate<T> predicate) {
    for (final T value : collection) {
      if (predicate.evaluate(value)) {
        return true;
      }
    }
    return false;
  }
}
