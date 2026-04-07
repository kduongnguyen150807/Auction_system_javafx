package com.auction.client.ui.WorldChat;

import com.auction.client.ClientSession;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ChatLabel.ChatLabelController;
import com.auction.shared.Request;
import com.auction.shared.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class WorldChatController {
    @FXML private Pane ChatContainer;
    @FXML private TextField MessageField;

    public void loadChatHistory() throws IOException {
        System.out.println("sending get history req");
        Request req = new Request(Request.GET_CHAT_HISTORY, null);
        Response res = NetworkClient.getInstance().sendRequestAndWait(req);

        List<HashMap<String,String>> chatHistory = (List<HashMap<String,String>>) res.getPayload();
        for(HashMap<String,String> msg : chatHistory){
            updateChat(msg);
        }
    }

    @FXML
    public void SendMessage(){
        String message = MessageField.getText();
        HashMap<String, String> data = new HashMap<>();
        data.put("message", message);
        data.put("username", ClientSession.getUsername());
        Request req = new Request(Request.SEND_MESSAGE, data);
        System.out.println("sending from controller");
        new Thread(() -> {
            System.out.println("Đang gửi tin nhắn từ luồng phụ...");
            NetworkClient.getInstance().sendRequestAndWait(req);

            Platform.runLater(() -> MessageField.clear());
        }).start();
    }

    public void updateChat(HashMap<String, String> data) throws IOException {
        NodeContentLoader<AnchorPane> ChatLabel = new NodeContentLoader<>();
        ChatLabel.load("/fxml/ChatLabel/ChatLabel.fxml");

        ChatLabelController clc = ChatLabel.getController();
        clc.setData(data.get("username"), data.get("message"));

        Platform.runLater(() -> {
            NodeManager.addNodeToPane(ChatLabel, ChatContainer);
        });
    }


}
