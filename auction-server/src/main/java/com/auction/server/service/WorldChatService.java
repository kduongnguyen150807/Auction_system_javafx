package com.auction.server.service;

import com.auction.server.controller.ClientHandler;
import com.auction.shared.Request;
import com.auction.shared.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class WorldChatService {
    private static WorldChatService instance;
    private List<ClientHandler> clients;
    private List<HashMap<String, String>> chatHistory;
    private final BlockingQueue<Request> messageQueue;

    public void addClient(ClientHandler c) { clients.add(c); }
    public void removeClient(ClientHandler c) { clients.remove(c); }

    private WorldChatService(){
        clients = new CopyOnWriteArrayList<>();
        chatHistory = new CopyOnWriteArrayList<>();
        messageQueue = new LinkedBlockingQueue<>();
    }

    public static synchronized WorldChatService getInstance(){
        if(instance==null){
            instance = new WorldChatService();
            instance.start();
        }
        return instance;
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Request req = messageQueue.take();
                    if(!(req.getPayload() instanceof HashMap<?,?>))return;
                    HashMap<String, String> data = (HashMap<String, String>) req.getPayload();
                    chatHistory.add(data);

                    String msg = data.get("message");
                    String username = data.get("username");
                    System.out.println(msg);
                    System.out.println(username);

                    Response res = new Response("", "NEW_MESSAGE", "newmsg", data);
                    broadcast(res);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }).start();
    }

    public void enqueueMessage(Request req) {
        messageQueue.offer(req);
        System.out.println("add to queue");
    }

    public List<HashMap<String, String>> getChatHistory(){
        return chatHistory;
    }

    public void broadcast(Response r){
        for(ClientHandler c: clients){
            c.send(r);
        }
    }

}
