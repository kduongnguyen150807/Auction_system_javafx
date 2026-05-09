package com.auction.client.network;

import com.auction.shared.Response;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

/** TCP connection exchanging serialized Java objects. Owns the background read loop only. */
final class ObjectSocketConnection {
  private final Socket socket;
  private final ObjectOutputStream out;
  private final ObjectInputStream in;

  private ObjectSocketConnection(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
    this.socket = socket;
    this.out = out;
    this.in = in;
  }

  static ObjectSocketConnection connect(String host, int port) throws IOException {
    Socket socket = new Socket(host, port);
    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
    out.flush();
    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
    return new ObjectSocketConnection(socket, out, in);
  }

  ObjectOutputStream getOut() {
    return out;
  }

  void startReadLoop(Consumer<Response> onResponse, Consumer<Throwable> onDisconnect) {
    Thread t =
        new Thread(
            () -> {
              try {
                while (true) {
                  Object obj = in.readObject();
                  if (obj instanceof Response response) onResponse.accept(response);
                }
              } catch (Exception e) {
                onDisconnect.accept(e);
              }
            },
            "NetworkClient-Listener");
    t.setDaemon(true);
    t.start();
  }
}
