package com.auction.client.Chat.client;

import com.auction.client.Chat.model.ChatMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

public class ChatController {
    @FXML private TextArea messageArea;
    @FXML private TextField inputField;

    private StompSession stompSession;
    private String currentUsername = "Unknown";

    @FXML
    public void initialize() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new org.springframework.messaging.converter.MappingJackson2MessageConverter());
        String url = "ws://localhost:8081/ws-chat";

        System.out.println("⏳ Đang thử kết nối tới Server Chat tại: " + url);

        stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                stompSession = session;
                System.out.println("✅ Đã kết nối Socket Chat thành công (Cổng 8081)!");

                stompSession.subscribe("/topic/messages", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ChatMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        ChatMessage msg = (ChatMessage) payload;
                        Platform.runLater(() -> messageArea.appendText(msg.getSender() + ": " + msg.getContent() + "\n"));
                    }
                });
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("❌ LỖI KẾT NỐI WEBSOCKET CHAT: " + exception.getMessage());
            }
        });
    }

    public void initUserData(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.currentUsername = username;
        }
    }

    @FXML
    protected void onSendButtonClick() {
        String content = inputField.getText();
        if (!content.isEmpty()) {
            // Kiểm tra xem đã kết nối chưa mới cho gửi
            if (stompSession != null && stompSession.isConnected()) {
                ChatMessage msg = new ChatMessage();
                msg.setSender(this.currentUsername);
                msg.setContent(content);

                stompSession.send("/app/chat", msg);
                inputField.clear();
            } else {
                System.err.println("⚠️ Chưa kết nối được tới Server Chat! Vui lòng kiểm tra lại Server.");
                Platform.runLater(() -> messageArea.appendText("Hệ thống: Mất kết nối tới máy chủ Chat!\n"));
            }
        }
    }
}