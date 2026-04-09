package com.auction.server.controller;

import com.auction.server.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class ChatServerController {

    private List<ChatMessage> lichSuChat = new CopyOnWriteArrayList<>();

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        System.out.println("SERVER NHẬN ĐƯỢC: " + message.getSender() + " gửi " + message.getContent());
        lichSuChat.add(message);
        if (lichSuChat.size() > 50) {
            lichSuChat.remove(0);
        }

        return message;
    }

    @GetMapping("/api/chat-history")
    public List<ChatMessage> getChatHistory() {
        return lichSuChat;
    }
}