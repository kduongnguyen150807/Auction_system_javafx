package com.example.demo;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Bidding extends Application {

    @Override
    public void start(Stage primaryStage) {

        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-color: #050505;");

        HBox topMenu = new HBox(30);
        Label loginLabel = createMenuLink("Đăng nhập");
        Label registerLabel = createMenuLink("Đăng ký");
        topMenu.getChildren().addAll(loginLabel, registerLabel);

        AnchorPane.setTopAnchor(topMenu, 40.0);
        AnchorPane.setRightAnchor(topMenu, 50.0);

        VBox leftContent = new VBox(25);
        leftContent.setAlignment(Pos.CENTER_LEFT);

        VBox logoBox = new VBox(5);
        logoBox.setAlignment(Pos.CENTER);

        Text logoIcon = new Text("B");
        logoIcon.setFont(Font.font("SansSerif", FontWeight.BOLD, 100));
        logoIcon.setFill(Color.web("#dca727"));
        Text logoText1 = new Text("BIDDING88");
        logoText1.setFont(Font.font("SansSerif", FontWeight.BOLD, 36));
        logoText1.setFill(Color.WHITE);

        Text logoText2 = new Text("SINCE 2026");
        logoText2.setFont(Font.font("SansSerif", FontWeight.BOLD, 22));
        logoText2.setFill(Color.web("#dca727"));

        logoBox.getChildren().addAll(logoIcon, logoText1, logoText2);

        Label subtitle = new Label("Online Auctions");
        subtitle.setFont(Font.font("Serif", 26));
        subtitle.setTextFill(Color.WHITE);

        leftContent.getChildren().addAll(logoBox, subtitle);

        AnchorPane.setTopAnchor(leftContent, 120.0);
        AnchorPane.setLeftAnchor(leftContent, 100.0);

        root.getChildren().addAll(topMenu, leftContent);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Bidding88 - Nền tảng đấu giá");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Label createMenuLink(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Serif", 22));
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-cursor: hand;");

        label.setOnMouseEntered(e -> label.setTextFill(Color.web("#dca727")));
        label.setOnMouseExited(e -> label.setTextFill(Color.WHITE));

        return label;
    }

    public static void main(String[] args) {
        launch(args);
    }
}