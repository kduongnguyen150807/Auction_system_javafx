package com.auction.server.controller;

import com.auction.server.Service.RequestDispatcher;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable{
  private Socket socket;
  private User currentUser;
  private ObjectOutputStream out;
  private ObjectInputStream in;

  public ClientHandler(Socket s){
    this.socket = s;
    try {
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
    } catch (Exception e) {
    }
  }


  @Override
  public void run(){
    try{
      while (true){
        Request req = (Request) this.in.readObject();

        Response res = RequestDispatcher.dispatch(req, this.currentUser);
        if(res!= null){
          if (Request.LOGIN.equals(req.getAction()) && res.getStatus() == Response.OK) {
            this.currentUser = (User) res.getPayload();
          }
          synchronized (this.out) {
            this.out.reset();
            this.out.writeObject(res);
            this.out.flush();
          }
        }
      }
    } catch (EOFException e) {
      System.out.println("Client chủ động ngắt kết nối (EOF).");
    } catch (SocketException e) {
      System.out.println("Kết nối với Client bị mất đột ngột.");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeResources();
    }
  }

  private void closeResources() {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
      if (socket != null && !socket.isClosed()) socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public User getCurrentUser(){
    return currentUser;
  }

  public void send(Response r) {
    try {
      synchronized (this.out) {
        this.out.reset();
        this.out.writeObject(r);
        this.out.flush();
      }
    } catch (Exception e) {
    }
  }
}