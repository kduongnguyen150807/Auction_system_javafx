package com.auction.server.controller;

import com.auction.server.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatServerController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        // Dòng này cực kỳ quan trọng để kiểm tra Server đã nhận được tin chưa
        System.out.println("SERVER NHẬN ĐƯỢC: " + message.getSender() + " gửi " + message.getContent());
        return message;
    }
}