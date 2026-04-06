package com.auction.client.controller;

import com.auction.client.Main;
import com.auction.client.SceneManager;
import com.auction.client.Service.BackGroundService;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static com.auction.client.Service.BackGroundService.updateConfigUrl;

public class Welcome2Controller {

    public void toLogin(ActionEvent e) throws Exception {
        NodeContentLoader<AnchorPane> login = new NodeContentLoader<>();
        login.load("/fxml/login.fxml");
        WelcomeController.getKhung().getChildren().clear();
        NodeManager.addNodeToPane(login, WelcomeController.getKhung());
    }

    public void toRegister(ActionEvent e) throws Exception {
        NodeContentLoader<AnchorPane> login = new NodeContentLoader<>();
        login.load("/fxml/register.fxml");
        WelcomeController.getKhung().getChildren().clear();
        NodeManager.addNodeToPane(login, WelcomeController.getKhung());
    }

    @FXML
    public void ChangeBg(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        // Thêm filter để người dùng chỉ chọn được ảnh
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File res = fc.showOpenDialog(null);

        // Kiểm tra nếu người dùng không chọn file (nhấn Cancel) thì không làm gì cả
        if (res != null) {
            try {
                // 1. Cập nhật URL mới vào file properties (dùng toURI() để tránh lỗi ký tự đặc biệt)
                updateConfigUrl(res.toURI().toString());

                // 2. Áp dụng ngay lập tức lên Pane hiện tại
                BackGroundService.apply(WelcomeController.getKhung());

                System.out.println("Đã thay đổi nền thành công!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    @FXML
    public void RandomBg(ActionEvent actionEvent) {
        // 1. Gọi hàm tạo link ngẫu nhiên và lưu vào file config
        BackGroundService.randomBg();

        // 2. Áp dụng ngay lập tức lên giao diện
        BackGroundService.apply(WelcomeController.getKhung());
    }
}