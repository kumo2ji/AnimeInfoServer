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

import com.ais.datastore.AnimeDatastore;
import com.ais.datastore.AnimeEntityInfo;
import com.ais.datastore.PeriodDatastore;
import com.ais.datastore.PeriodEntityInfo;
import com.ais.external.AnimeBaseObject;
import com.ais.external.CoursObject;
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
    AnimeDatastore.delete();
    return new BooleanResponse(true);
  }

  @ApiMethod(name = "auth.delete.period.all", path = "delete/period/all",
      httpMethod = HttpMethod.GET, scopes = {ApiConstants.EMAIL_SCOPE},
      clientIds = {ApiConstants.WEB_CLIENT_ID})
  public BooleanResponse deleteAllPeriod() {
    PeriodDatastore.delete();
    return new BooleanResponse(true);
  }

  @ApiMethod(name = "auth.connect.external.put.all", path = "connect/all",
      httpMethod = HttpMethod.GET, scopes = {ApiConstants.EMAIL_SCOPE},
      clientIds = {ApiConstants.WEB_CLIENT_ID})
  public BooleanResponse connectExternalAndPutAll() throws InternalServerErrorException {
    try {
      final Map<String, CoursObject> coursMap = ExternalAnimeInfoUtils.requestCoursObjectMap();
      final Collection<CoursObject> coursObjects = coursMap.values();
      PeriodDatastore.put(coursObjects);
      final List<AnimeBaseObject> list = new ArrayList<AnimeBaseObject>();
      for (final CoursObject coursObject : coursObjects) {
        list.addAll(ExternalAnimeInfoUtils.requestAnimeBaseObjects(coursObject.getYear(),
            coursObject.getCours()));
      }
      AnimeDatastore.putAnimeBaseObjects(list);
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
      PeriodDatastore.put(coursObjects);
      final CoursObject current = Collections.max(coursObjects, new Comparator<CoursObject>() {
        @Override
        public int compare(final CoursObject o1, final CoursObject o2) {
          return (int) (o1.getId() - o2.getId());
        }
      });
      AnimeDatastore.putAnimeBaseObjects(
          ExternalAnimeInfoUtils.requestAnimeBaseObjects(current.getYear(), current.getCours()));
    } catch (final IOException e) {
      throw new InternalServerErrorException(e);
    }
    return new BooleanResponse(true);
  }

  @ApiMethod(path = "get/anime", name = "get.anime", httpMethod = HttpMethod.POST)
  public CollectionResponse<AnimeInfoBean> getAnimeInfoBeans(final GetAnimeInfoRequest request) {
    final PreparedQuery preparedQuery = queryAnimeInfo(request.getPeriod());
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
    final List<Key> keys = AnimeDatastore.put(request.getItems());
    final Map<Key, Entity> map = AnimeDatastore.query(keys);
    final Collection<AnimeInfoBean> putBeans = CollectionUtils.collect(map.values(),
        animeEntityInfo.getEntityToAnimeInfoBeanTransformer());
    final Builder<AnimeInfoBean> builder = CollectionResponse.<AnimeInfoBean>builder();
    builder.setItems(putBeans);
    return builder.build();
  }

  @ApiMethod(name = "erase.anime", path = "erase/anime", httpMethod = HttpMethod.POST)
  public BooleanResponse deleteAnimeInfo(final IdRequest request) {
    AnimeDatastore.delete(request.getIds());
    return new BooleanResponse(true);
  }

  @ApiMethod(path = "get/period", name = "get.period")
  public Collection<PeriodBean> getPeriodBeans() {
    final PeriodEntityInfo entityInfo = new PeriodEntityInfo();
    final PreparedQuery preparedQuery = PeriodDatastore.query();
    return CollectionUtils.collect(preparedQuery.asIterable(),
        entityInfo.getEntityToPeriodBeanTransformer());
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

  private PreparedQuery queryAnimeInfo(final PeriodBean periodBean) {
    if (periodBean != null) {
      if (periodBean.getId() > 0) {
        return AnimeDatastore.queryWithPeriodId(periodBean.getId());
      } else {
        if (periodBean.getYear() > 2000) {
          if (periodBean.getSeason() > 0) {
            return AnimeDatastore.query(periodBean.getYear(), periodBean.getSeason());
          } else {
            return AnimeDatastore.query(periodBean.getYear());
          }
        }
      }
    }
    return AnimeDatastore.query();
  }
}
