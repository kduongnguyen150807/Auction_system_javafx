package com.auction.client.Chat.client;

import com.auction.client.Chat.model.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ChatController {
    @FXML private TextArea messageArea;
    @FXML private TextField inputField;

    private StompSession stompSession;
    private String currentUsername = "Unknown";

    private final String SERVER_IP = "localhost";

    @FXML
    public void initialize() {
        System.out.println("⏳ Giao diện Chat đã sẵn sàng, đang chờ nạp dữ liệu...");
    }

    public void initUserData(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.currentUsername = username;
        }
        loadChatHistory();
    }

    private void loadChatHistory() {
        new Thread(() -> {
            try {
                String apiUrl = "http://" + SERVER_IP + ":8081/api/chat-history";
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String jsonLichSu = response.body();

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ChatMessage>>() {}.getType();
                    List<ChatMessage> history = gson.fromJson(jsonLichSu, listType);

                    Platform.runLater(() -> {
                        for (ChatMessage msg : history) {
                            messageArea.appendText(msg.getSender() + ": " + msg.getContent() + "\n");
                        }
                        messageArea.appendText("--- Bắt đầu tin nhắn mới ---\n");

                        connectWebSocket();
                    });
                } else {
                    Platform.runLater(this::connectWebSocket);
                }
            } catch (Exception e) {
                System.out.println("❌ Không thể tải lịch sử chat! Lỗi: " + e.getMessage());
                Platform.runLater(this::connectWebSocket);
            }
        }).start();
    }

    private void connectWebSocket() {
        if (stompSession != null && stompSession.isConnected()){
            return;
        }
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new org.springframework.messaging.converter.MappingJackson2MessageConverter());
        String url = "ws://" + SERVER_IP + ":8081/ws-chat";

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

    @FXML
    protected void onSendButtonClick() {
        String content = inputField.getText();
        if (!content.isEmpty()) {
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