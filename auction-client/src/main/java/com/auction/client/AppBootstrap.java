package com.auction.client;

import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.network.NetworkClient;
import com.auction.client.network.NotificationDispatcher;
import com.auction.client.service.admin.AdminService;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.service.auction.BiddingService;
import com.auction.client.service.auction.LotManagementService;
import com.auction.client.service.item.ItemService;
import com.auction.client.service.user.AuthService;
import com.auction.client.service.user.ClientService;
import com.auction.client.service.user.UserService;
import com.auction.client.ui.loginview.LoginView;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class AppBootstrap {
  private final Map<Class<?>, Object> services = new HashMap<>();

  public static void run(Stage stage) {
    SceneManager sceneManager = SceneManager.getInstance();
    sceneManager.setStage(stage);

    /* khoi tao scene */
    Scene loginViewScene = new Scene(new LoginView());

    /* register scene */
    sceneManager.registerScene(SceneType.LOGIN_VIEW, loginViewScene);

    sceneManager.switchScene(SceneType.LOGIN_VIEW);

    /* service */
    AuctionDiscoveryService auctionDiscoveryService = new AuctionDiscoveryService();
    AuctionDetailService auctionDetailService = new AuctionDetailService();
    AuthService authService = new AuthService();
    LotManagementService lotManagementService = new LotManagementService();

    /* khoi tao NotificationDispatcher */
    NotificationDispatcher notificationDispatcher =
      new NotificationDispatcher(auctionDetailService, authService, auctionDiscoveryService);

    /* add NotificationDispatcher */
    NetworkClient.getInstance().addListener(notificationDispatcher);

    /* register service */
    AppContainer.registerService(AdminService.class, new AdminService());
    AppContainer.registerService(BiddingService.class, new BiddingService());

    AppContainer.registerService(AuctionDetailService.class, auctionDetailService);
    AppContainer.registerService(AuctionDiscoveryService.class, auctionDiscoveryService);
    AppContainer.registerService(UserService.class, new UserService());

    AppContainer.registerService(AuthService.class, authService);
    AppContainer.registerService(UserService.class, new UserService());
    AppContainer.registerService(ClientService.class, new ClientService());
    AppContainer.registerService(ItemService.class, new ItemService());

    AppContainer.registerService(LotManagementService.class, lotManagementService);
  }
}
