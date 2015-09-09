package com.ais.utils;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {
  private static final Gson gson = new Gson();

  private JsonUtils() {}

  public static <T> Map<String, T> toMapFromJson(final Reader reader, final Class<T> classOfT) {
    final JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
    final Transformer<JsonElement, T> transformer = getJsonElementTransformer(classOfT);
    final Map<String, T> map = new HashMap<String, T>();
    for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      final T instance = transformer.transform(entry.getValue());
      map.put(entry.getKey(), instance);
    }
    return map;
  }

  public static <T> Transformer<Reader, Map<String, T>> getFuncForMap(final Class<T> classOfT) {
    return new Transformer<Reader, Map<String, T>>() {
      @Override
      public Map<String, T> transform(final Reader arg) {
        return toMapFromJson(arg, classOfT);
      }
    };
  }

  public static <T> Collection<T> toCollectionFromJson(final Reader reader,
      final Class<T> classOfT) {
    final JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
    return CollectionUtils.collect(jsonArray, getJsonElementTransformer(classOfT));
  }

  private static <T> Transformer<JsonElement, T> getJsonElementTransformer(
      final Class<T> classOfT) {
    return new Transformer<JsonElement, T>() {
      @Override
      public T transform(final JsonElement input) {
        return gson.fromJson(input, classOfT);
      }
    };
  }

  public static <T> Transformer<Reader, Collection<T>> getFuncForCollection(
      final Class<T> classOfT) {
    return new Transformer<Reader, Collection<T>>() {
      @Override
      public Collection<T> transform(final Reader arg) {
        return toCollectionFromJson(arg, classOfT);
      }
    };
  }
}
