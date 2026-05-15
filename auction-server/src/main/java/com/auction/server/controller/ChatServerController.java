package com.auction.server.controller;

import com.auction.server.model.ChatMessage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatServerController {
    private static final Logger logger = LoggerFactory.getLogger(ChatServerController.class);
    private static final int MAX_CHAT_HISTORY_SIZE = 50;

    private final List<ChatMessage> chatHistory = new CopyOnWriteArrayList<>();

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        logger.info("received_chat_message_from_{}", message.getSender());

        chatHistory.add(message);

        if (chatHistory.size() > MAX_CHAT_HISTORY_SIZE) {
            chatHistory.remove(0);
        }

        return message;
    }

    @GetMapping("/api/chat-history")
    public List<ChatMessage> getChatHistory() {
        return chatHistory;
    }
}