package com.auction.client.ui.ItemCard;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import com.auction.client.ui.Main.KhungController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ItemCardController {
  @FXML private VBox itemRoot;
  @FXML private Label ItemName, ItemDescription, Price, TimeRemain;
  @FXML private ImageView ImageHolder;

  private int id;
  private String n, d, t, u, sn, sa;
  private double p;

  public void setData(
      int iid,
      String iname,
      double ip,
      String idesc,
      String it,
      String iurl,
      String isn,
      String isa) {
    this.id = iid;
    this.n = iname;
    this.p = ip;
    this.d = idesc;
    this.t = it;
    this.u = iurl;
    this.sn = isn;
    this.sa = isa;

    if (ItemName != null) ItemName.setText(this.n);
    if (ItemDescription != null) ItemDescription.setText(this.d);
    if (Price != null) Price.setText(String.format("%,.0f$", this.p));
    if (TimeRemain != null) TimeRemain.setText(this.t);

    if (ImageHolder != null && this.u != null && !this.u.isBlank()) {
      Image img = new Image(this.u, true);
      img.progressProperty()
          .addListener(
              (obs, oldv, newv) -> {
                if (newv.doubleValue() == 1.0) {
                  Platform.runLater(() -> applyCenterCrop(ImageHolder, img));
                }
              });
      ImageHolder.setImage(img);
    }
  }

  private void applyCenterCrop(ImageView iv, Image img) {
    double w = img.getWidth();
    double h = img.getHeight();
    double targetW = iv.getFitWidth();
    double targetH = iv.getFitHeight();

    double imgRatio = w / h;
    double targetRatio = targetW / targetH;

    double cropW, cropH, cropX, cropY;
    if (imgRatio > targetRatio) {
      cropH = h;
      cropW = h * targetRatio;
      cropX = (w - cropW) / 2;
      cropY = 0;
    } else {
      cropW = w;
      cropH = w / targetRatio;
      cropX = 0;
      cropY = (h - cropH) / 2;
    }

    iv.setViewport(new Rectangle2D(cropX, cropY, cropW, cropH));
  }

  public void updatePrice(double res) {
    this.p = res;
    if (Price != null) Price.setText(String.format("%,.0f$", res));
  }

  public int getId() {
    int res = this.id;
    return res;
  }

  public void handleItemClicked() {
    try {
      NodeContentLoader<ScrollPane> l = new NodeContentLoader<>();
      l.load("/fxml/iteminformation/ItemInformation.fxml");
      ItemInformationController c = l.getController();
      if (c != null) {
        c.setData(id, n, p, 0, d, t, u, sn, sa);
        c.refresh();
        KhungController.itemDetailController = c;
      }
      NodeManager.switchNodewithNode(
          l.getCurrentNode(),
          KhungController.getCurrentNode(),
          KhungController.getMainContentPane());
      KhungController.setMainContentNode(l.getCurrentNode());
    } catch (Exception e) {
    }
  }
}
