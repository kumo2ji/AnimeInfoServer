package com.ais.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.ais.datastore.AnimeEntityInfo;
import com.ais.datastore.CoursEntityInfo;
import com.ais.datastore.DatastoreUtils;
import com.ais.external.AnimeBaseObject;
import com.ais.external.ExternalAnimeInfoUtils;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.CollectionResponse.Builder;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultList;

@Api(name = "animeInfo")
public class AnimeInfoApi {
  // private static final Logger logger = Logger.getLogger(AnimeInfoApi.class.getName());
  private static final AnimeEntityInfo animeEntityInfo = new AnimeEntityInfo();

  @ApiMethod(name = "auth.delete.anime.all", path = "delete/anime/all", httpMethod = HttpMethod.GET,
      scopes = {ApiConstants.EMAIL_SCOPE}, clientIds = {ApiConstants.WEB_CLIENT_ID})
  public BooleanResponse deleteAllAnimeInfo() {
    DatastoreUtils.deleteAnimeInfo();
    return new BooleanResponse(true);
  }

  @ApiMethod(name = "auth.delete.cours.all", path = "delete/cours/all", httpMethod = HttpMethod.GET,
      scopes = {ApiConstants.EMAIL_SCOPE}, clientIds = {ApiConstants.WEB_CLIENT_ID})
  public BooleanResponse deleteCoursObject() {
    DatastoreUtils.deleteCoursObject();
    return new BooleanResponse(true);
  }

  @ApiMethod(name = "auth.connect.external.put.all", path = "connect/all",
      httpMethod = HttpMethod.GET, scopes = {ApiConstants.EMAIL_SCOPE},
      clientIds = {ApiConstants.WEB_CLIENT_ID})
  public BooleanResponse connectExternalAndPutAll() throws InternalServerErrorException {
    try {
      final Map<String, CoursObject> coursMap = ExternalAnimeInfoUtils.requestCoursObjectMap();
      final List<Key> coursKeys = DatastoreUtils.putCoursObjects(coursMap.values());
      if (CollectionUtils.isEmpty(coursKeys)) {
        return new BooleanResponse(false, "failed to store coursObject");
      }
      final List<AnimeBaseObject> list = new ArrayList<AnimeBaseObject>();
      for (final CoursObject coursObject : coursMap.values()) {
        final String year = String.valueOf(coursObject.getYear());
        final String cours = String.valueOf(coursObject.getCours());
        list.addAll(ExternalAnimeInfoUtils.requestAnimeBaseObjects(year, cours));
      }
      final List<Key> animeKeys = DatastoreUtils.putAnimeBaseObjects(list, coursMap);
      if (CollectionUtils.isEmpty(animeKeys)) {
        return new BooleanResponse(false, "failed to store animeBaseObject");
      }
    } catch (final IOException e) {
      throw new InternalServerErrorException(e);
    }
    return new BooleanResponse(true);
  }

  @ApiMethod(name = "auth.connect.external.put.current", path = "connect/current",
      httpMethod = HttpMethod.GET, scopes = {ApiConstants.EMAIL_SCOPE},
      clientIds = {ApiConstants.WEB_CLIENT_ID})
  public BooleanResponse connectExternalAndPutCurrent() throws InternalServerErrorException {
    try {
      final Map<String, CoursObject> coursMap = ExternalAnimeInfoUtils.requestCoursObjectMap();
      final Collection<CoursObject> coursObjects = coursMap.values();
      if (CollectionUtils.isEmpty(coursObjects)) {
        return new BooleanResponse(false, "failed to connect external server");
      }
      DatastoreUtils.putCoursObjects(coursObjects);
      final CoursObject current = Collections.max(coursObjects, new Comparator<CoursObject>() {
        @Override
        public int compare(final CoursObject o1, final CoursObject o2) {
          return (int) (o1.getId() - o2.getId());
        }
      });
      final String year = String.valueOf(current.getYear());
      final String cours = String.valueOf(current.getCours());
      DatastoreUtils.putAnimeBaseObjects(
          ExternalAnimeInfoUtils.requestAnimeBaseObjects(year, cours), coursMap);
    } catch (final IOException e) {
      throw new InternalServerErrorException(e);
    }
    return new BooleanResponse(true);
  }

  @ApiMethod(path = "get/anime", name = "get.anime", httpMethod = HttpMethod.POST)
  public CollectionResponse<AnimeInfoBean> getAnimeInfoBeans(final GetAnimeInfoRequest request) {
    final PreparedQuery preparedQuery = queryAnimeInfo(request.getCoursObject());
    final FetchOptions options = createFetchOptions(request.getLimit(), request.getCursor());
    final QueryResultList<Entity> entityList = preparedQuery.asQueryResultList(options);
    final Collection<AnimeInfoBean> beans =
        CollectionUtils.collect(entityList, animeEntityInfo.getEntityToAnimeInfoBeanTransformer());
    final Builder<AnimeInfoBean> builder = CollectionResponse.<AnimeInfoBean>builder();
    builder.setItems(beans);
    final Cursor cursor = entityList.getCursor();
    if (cursor != null) {
      builder.setNextPageToken(cursor.toWebSafeString());
    }
    return builder.build();
  }

  @ApiMethod(path = "put/anime", name = "put.anime", httpMethod = HttpMethod.POST)
  public CollectionResponse<AnimeInfoBean> putAnimeInfoBeans(final PostAnimeInfoRequest request) {
    final List<Key> keys = DatastoreUtils.putAnimeInfoBeans(request.getBeans());
    final Map<Key, Entity> map = DatastoreUtils.queryAnimeInfoBean(keys);
    final Collection<AnimeInfoBean> putBeans = CollectionUtils.collect(map.values(),
        animeEntityInfo.getEntityToAnimeInfoBeanTransformer());
    final Builder<AnimeInfoBean> builder = CollectionResponse.<AnimeInfoBean>builder();
    builder.setItems(putBeans);
    return builder.build();
  }

  @ApiMethod(name = "delete.anime", path = "delete/anime", httpMethod = HttpMethod.POST)
  public BooleanResponse deleteAnimeInfo(final IdRequest request) {
    DatastoreUtils.deleteAnimeInfos(request.getIds());
    return new BooleanResponse(true);
  }

  @ApiMethod(path = "get/cours", name = "get.cours")
  public Collection<CoursObject> getCoursObjects() {
    final CoursEntityInfo entityInfo = new CoursEntityInfo();
    final PreparedQuery preparedQuery = DatastoreUtils.queryCoursObject();
    return CollectionUtils.collect(preparedQuery.asIterable(),
        entityInfo.getEntityToCoursObjectTransformer());
  }

  private FetchOptions createFetchOptions(final int limit, final String cursorString) {
    final FetchOptions options = FetchOptions.Builder.withDefaults();
    if (limit > 0) {
      options.limit(limit);
    } else {
      options.limit(100);
    }
    if (!StringUtils.isEmpty(cursorString)) {
      final Cursor cursor = Cursor.fromWebSafeString(cursorString);
      options.startCursor(cursor);
    }
    return options;
  }

  private PreparedQuery queryAnimeInfo(final CoursObject coursObject) {
    if (coursObject != null) {
      if (coursObject.getId() > 0) {
        return DatastoreUtils.queryAnimeBaseObjectsWithId(coursObject.getId());
      } else {
        if (coursObject.getYear() > 2000) {
          if (coursObject.getCours() > 0) {
            return DatastoreUtils.queryAnimeInfoBeans(coursObject.getYear(),
                coursObject.getCours());
          } else {
            return DatastoreUtils.queryAnimeInfoBeans(coursObject.getYear());
          }
        }
      }
    }
    return DatastoreUtils.queryAnimeInfoBeans();
  }
}
