package com.auction.client.app;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.auction.client.AppBootstrap;
import com.auction.client.AppContainer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeContentLoader<T extends Node> {
  private static final Logger LOGGER = LoggerFactory.getLogger(NodeContentLoader.class);

  private T currentNode;
  private Object controller;


  public void load(String fxmlPath) throws IOException {
    URL location = getClass().getResource(fxmlPath);
    if (location == null) {
      throw new IOException("Không tìm thấy file FXML tại đường dẫn: " + fxmlPath);
    }

    FXMLLoader loader = new FXMLLoader(location);

    loader.setControllerFactory(controllerClass -> {
      try {
        for (Constructor<?> constructor : controllerClass.getConstructors()) {
          if (constructor.isAnnotationPresent(AutoInject.class)) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] arg = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
              arg[i] = AppContainer.getService(parameterTypes[i]);
              if (arg[i] == null) {
                LOGGER.warn("Thiếu Dependency: {} cho Controller: {}", parameterTypes[i].getName(), controllerClass.getName());
              }
            }
            return constructor.newInstance(arg);
          }
        }

        return controllerClass.getDeclaredConstructor().newInstance();

      } catch (Exception e) {
        LOGGER.error("Auto-inject failed for: {}", controllerClass.getName(), e);
        throw new RuntimeException("Lỗi nghiêm trọng khi khởi tạo Controller qua DI Factory: " + controllerClass.getName(), e);
      }
    });

    currentNode = loader.load();
    controller = loader.getController();
  }

  public T getCurrentNode() {
    return currentNode;
  }

  @SuppressWarnings("unchecked")
  public <C> C getController() {
    return (C) controller;
  }
}