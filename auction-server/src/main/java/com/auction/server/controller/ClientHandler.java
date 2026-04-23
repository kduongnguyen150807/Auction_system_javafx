package com.auction.server.controller;

import com.auction.server.service.AuctionManager;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
  private final Socket socket;
  private final ObjectOutputStream out;
  private final ObjectInputStream in;
  private final RequestDispatcher dispatcher;
  private User currentUser;

  public ClientHandler(Socket socket, RequestDispatcher dispatcher) throws Exception {
    this.socket = socket;
    this.dispatcher = dispatcher;
    this.out = new ObjectOutputStream(socket.getOutputStream());
    this.out.flush();
    this.in = new ObjectInputStream(socket.getInputStream());
    AuctionManager.getInstance().addClient(this);
  }

  @Override
  public void run() {
    try {
      while (true) {
        Request req = (Request) in.readObject();
        Response res = dispatcher.dispatch(req, this);
        if (res != null) {
          send(res);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      AuctionManager.getInstance().removeClient(this);
      try {
        socket.close();
      } catch (Exception ignored) {
      }
    }
  }

  public synchronized void send(Response r) {
    try {
      out.reset();
      out.writeObject(r);
      out.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(User currentUser) {
    this.currentUser = currentUser;
  }
}