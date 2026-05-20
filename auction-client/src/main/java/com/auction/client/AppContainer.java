package com.auction.client;

import com.auction.client.app.AutoInject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class AppContainer {
  private static final Map<Class<?>, Object> SERVICES = new HashMap<>();

  public static void registerService(Class<?> serviceClass, Object instance) {
    SERVICES.put(serviceClass, instance);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getService(Class<T> serviceClass) {
    return (T) SERVICES.get(serviceClass);
  }

  public static void injectFields(Object target) {
    try {
      for (Field field : target.getClass().getDeclaredFields()) {
        if (field.isAnnotationPresent(AutoInject.class)) {
          field.setAccessible(true);
          Object service = SERVICES.get(field.getType());
          if (service != null) {
            field.set(target, service);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Field Injection thất bại cho component: " + target.getClass().getName(), e);
    }
  }
}
