package com.auction.server.controller;

import com.auction.server.handler.auction.AddLotHandler;
import com.auction.server.handler.auction.AutocompleteHandler;
import com.auction.server.handler.auction.BidHandler;
import com.auction.server.handler.auction.ItemQueryHandler;
import com.auction.server.handler.auction.ListItemsHandler;
import com.auction.server.handler.auction.LotQueryHandler;
import com.auction.server.handler.auction.SellerCancelItemHandler;
import com.auction.server.handler.auction.SellerUpdatePendingItemHandler;
import com.auction.server.handler.user.WatchlistHandler;
import com.auction.server.handler.auth.ForgotPasswordHandler;
import com.auction.server.handler.auth.LoginHandler;
import com.auction.server.handler.auth.ReconnectHandler;
import com.auction.server.handler.auth.SignupHandler;
import com.auction.server.handler.chat.ChatHandler;
import com.auction.server.handler.chat.FriendHandler;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.ActionRegistry;
import com.auction.server.handler.misc.LeaderboardHandler;
import com.auction.server.handler.misc.MiscHandler;
import com.auction.server.handler.rating.RatingHandler;
import com.auction.server.handler.user.DepositHandler;
import com.auction.server.handler.user.UpdateAvatarHandler;
import com.auction.server.handler.user.UpdateProfileHandler;
import com.auction.server.handler.user.UserManagementHandler;
import com.auction.shared.Request;

public final class ActionRegistryFactory {

    private ActionRegistryFactory() {}

    public static ActionRegistry create() {
        ActionRegistry registry = new ActionRegistry();

        registry.register(Request.LOGIN, new LoginHandler());
        registry.register(Request.SIGNUP, new SignupHandler());
        registry.register(Request.RECONNECT, new ReconnectHandler());

        ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler();
        registry.register(Request.FORGOT_PASSWORD_REQ, forgotPasswordHandler);
        registry.register(Request.FORGOT_PASSWORD_RESET, forgotPasswordHandler);

        registry.register(Request.AUTOCOMPLETE, new AutocompleteHandler());

        ListItemsHandler listItemsHandler = new ListItemsHandler();
        registry.register(Request.LIST, listItemsHandler);
        registry.register(Request.GET_ONGOING_LOTS, listItemsHandler);

        LotQueryHandler lotQueryHandler = new LotQueryHandler();
        registry.register(Request.GET_ONGOING_BIDS, lotQueryHandler);
        registry.register(Request.GET_TRENDING_LOTS, lotQueryHandler);
        registry.register(Request.GET_UPCOMING_BIDS, lotQueryHandler);
        registry.register(Request.GET_CLOSED_BIDS, lotQueryHandler);
        registry.register(Request.GET_PAST_BIDS, lotQueryHandler);
        registry.register(Request.GET_WATCHLIST_ITEMS, lotQueryHandler);

        ItemQueryHandler itemQueryHandler = new ItemQueryHandler();
        registry.register(Request.GET_MY_ITEMS, itemQueryHandler);
        registry.register(Request.GET_ITEM_BY_ID, itemQueryHandler);

        RatingHandler ratingHandler = new RatingHandler();
        registry.register(Request.GET_RATINGS, ratingHandler);

        MiscHandler miscHandler = new MiscHandler();
        registry.register(Request.REFRESH_USER, miscHandler);
        registry.register(Request.GET_TRANSACTIONS, miscHandler);
        registry.register(Request.GET_BID_HISTORY, miscHandler);
        registry.register(Request.PING, miscHandler);

        ChatHandler chatHandler = new ChatHandler();
        registry.register(Request.GET_GLOBAL_CHAT_HISTORY, chatHandler);
        registry.register(Request.GET_PRIVATE_CHAT_HISTORY, chatHandler);
        registry.register(Request.GET_CHAT_CONTACTS, chatHandler);

        UserManagementHandler userManagementHandler = new UserManagementHandler();
        registry.register(Request.GET_ALL_USERS, userManagementHandler);
        registry.register(Request.SEARCH_USERS, userManagementHandler);
        registry.register(Request.GET_USER_BY_ID, userManagementHandler);

        FriendHandler friendHandler = new FriendHandler();
        registry.register(Request.GET_FRIENDS, friendHandler);
        registry.register(Request.GET_FRIEND_REQUESTS, friendHandler);

        WatchlistHandler watchlistHandler = new WatchlistHandler();
        registry.register(Request.GET_WATCHLIST, ActionHandler.requireAuth(watchlistHandler));
        registry.register(Request.TOGGLE_WATCHLIST, ActionHandler.requireAuth(watchlistHandler));

        registry.register(Request.BID, ActionHandler.requireAuth(new BidHandler()));
        registry.register(Request.ADD_LOT, ActionHandler.requireAuth(new AddLotHandler()));
        registry.register(Request.SELLER_CANCEL_ITEM, ActionHandler.requireAuth(new SellerCancelItemHandler()));
        registry.register(
                Request.SELLER_UPDATE_PENDING_ITEM,
                ActionHandler.requireAuth(new SellerUpdatePendingItemHandler()));
        registry.register(Request.UPDATE_PROFILE, ActionHandler.requireAuth(new UpdateProfileHandler()));
        registry.register(Request.UPDATE_AVATAR, ActionHandler.requireAuth(new UpdateAvatarHandler()));
        registry.register(Request.DEPOSIT, ActionHandler.requireAuth(new DepositHandler()));
        registry.register(Request.SUBMIT_RATING, ActionHandler.requireAuth(ratingHandler));
        registry.register(Request.SEND_CHAT, ActionHandler.requireAuth(chatHandler));

        registry.register(Request.ADD_FRIEND, ActionHandler.requireAuth(friendHandler));
        registry.register(Request.ACCEPT_FRIEND, ActionHandler.requireAuth(friendHandler));
        registry.register(Request.DECLINE_FRIEND, ActionHandler.requireAuth(friendHandler));
        registry.register(Request.REMOVE_FRIEND, ActionHandler.requireAuth(friendHandler));

        registry.register(Request.LOCK_USER, ActionHandler.requireAdmin(userManagementHandler));
        registry.register(Request.UNLOCK_USER, ActionHandler.requireAdmin(userManagementHandler));
        registry.register(Request.PROMOTE_ADMIN, ActionHandler.requireAdmin(userManagementHandler));

        registry.register(Request.GET_PENDING_ITEMS, ActionHandler.requireAdmin(itemQueryHandler));
        registry.register(Request.APPROVE_ITEM, ActionHandler.requireAdmin(itemQueryHandler));
        registry.register(Request.REJECT_ITEM, ActionHandler.requireAdmin(itemQueryHandler));

        registry.register(Request.GET_STATUS_STATS, ActionHandler.requireAdmin(miscHandler));
        registry.register(Request.GET_CATEGORY_STATS, ActionHandler.requireAdmin(miscHandler));

        registry.register(Request.GET_LEADERBOARD, new LeaderboardHandler());

        return registry;
    }
}