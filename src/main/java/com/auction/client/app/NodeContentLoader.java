package com.auction.client.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

public class NodeContentLoader<T extends Node> {
    private T currentNode;
    private Object controller;
    private FXMLLoader loader;

    public void load(String fxmlPath) throws IOException {
        URL location = getClass().getResource(fxmlPath);
        loader = new FXMLLoader(location);

        //lay currentnode va controller
        currentNode = loader.load();
        this.controller = loader.getController();
    }

    public T getCurrentNode() {
        return currentNode;
    }

    @SuppressWarnings("unchecked")
    public <C> C getController() {
        return (C) controller;
    }

    public boolean isReady() {
        return currentNode != null;
    }
}
