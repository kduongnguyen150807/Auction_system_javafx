package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.userinformation.SelectedUser;
import com.auction.client.store.userinformation.UserModel;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.component.UserCard;
import com.auction.client.ui.component.itemcard.ItemCardConfig;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ReactiveFlowPane;
import com.auction.shared.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UserInformationController implements CanSwitchNode<HomeViewType> {
  @FXML private UserCard userCard;
  @FXML private Label itemsBoughtLabel;
  @FXML private Label itemsSoldLabel;
  @FXML private Label emailLabel;
  @FXML private ReactiveFlowPane<ItemModel> itemsContainer;

  private Consumer<HomeViewType> switchNode;

  private AuctionDetailService detailService;
  private ClientService clientService;

  Consumer<ItemModel> onCardClicked = (selectedItem) -> {
    detailService.setSelectedItem(selectedItem);
    switchNode.accept(HomeViewType.ITEM_INFORMATION);
  };
  Consumer<ItemModel> onHeartClicked = (selectedItem) -> {
    int itemId = selectedItem.getId();
    boolean isWatching = ClientSession.CURRENT_SESSION.getWatchedItemsList().contain(itemId);
    clientService.toggleWatchedItem(itemId, !isWatching);
  };
  private final ItemCardConfig itemCardConfig = new ItemCardConfig(onHeartClicked, onCardClicked);

  @AutoInject
  public UserInformationController(AuctionDetailService detailService, ClientService clientService) {
    this.detailService = detailService;
    this.clientService = clientService;
  }

  @FXML
  public void initialize() {
    SelectedUser.SELECTED_USER.selectedUserProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        apply(newValue);
      }
    });

    apply(SelectedUser.SELECTED_USER.getSelectedUser());
  }

  private void apply(UserModel userModel) {
    userCard.setUserModel(userModel);
    itemsBoughtLabel.setText(String.valueOf(userModel.getUser().getItemsBought()));
    itemsSoldLabel.setText(String.valueOf(userModel.getUser().getItemsSold()));
    emailLabel.setText(userModel.getUser().getEmail());

    detailService.getSelectedUserItems(userModel.getUser().getId())
      .thenCompose(list -> {
        List<ItemModel> items = new ArrayList<ItemModel>();
        for (Item item: list) {
          items.add(new ItemModel(item));
        }

        itemsContainer.setAll(items, itemCardConfig.cardFactory());
        return null;
      });
  }

  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}
