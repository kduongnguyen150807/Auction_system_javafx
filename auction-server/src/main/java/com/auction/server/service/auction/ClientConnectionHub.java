package com.auction.server.service.auction;

import com.auction.server.controller.ClientHandler;
import com.auction.shared.Response;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Holds connected clients and fans out {@link Response}s. */
final class ClientConnectionHub {

  private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

  void addClient(ClientHandler client) {
    clients.add(client);
  }

  void removeClient(ClientHandler client) {
    clients.remove(client);
  }

  void broadcast(Response response) {
    clients.forEach(c -> c.send(response));
  }

  void sendToUser(int userId, Response response) {
    clients.stream()
        .filter(c -> c.getCurrentUser() != null && c.getCurrentUser().getId() == userId)
        .forEach(c -> c.send(response));
  }
}
