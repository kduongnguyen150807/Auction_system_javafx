package com.auction.server.context;

import com.auction.server.dao.BaseDao;
import com.auction.server.dao.LotDao;
import com.auction.server.dao.UserDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoContext {
  private final Map<Class<?>, Object> daoMap = new HashMap<>();

  public void injectDao(Object dao) {
    daoMap.put(dao.getClass(), dao);
  }

  public <T> T getDao(Class<T> clazz) {
    return clazz.cast(daoMap.get(clazz));
  }
}