<<<<<<< HEAD
<<<<<<< HEAD
package com.auction.server.service;
import com.auction.server.network.ClientHandler;
import com.auction.shared.Response;
import java.util.Vector;
public class ObserverManager {
    private static Vector<ClientHandler> clients = new Vector<>();
    public static void add(ClientHandler c) {
        clients.add(c);
    }
    public static void remove(ClientHandler c) {
        clients.remove(c);
    }
    public static void broadcast(Response res) {
        for (ClientHandler c : clients) {
            c.send(res);
        }
    }
}
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
