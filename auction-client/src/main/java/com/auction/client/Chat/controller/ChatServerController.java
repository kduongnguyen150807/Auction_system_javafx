package com.auction.client.Chat.controller;

import com.auction.client.Chat.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatServerController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        System.out.println("SERVER NHẬN ĐƯỢC: " + message.getSender() + " gửi " + message.getContent());
        return message;
    }
}