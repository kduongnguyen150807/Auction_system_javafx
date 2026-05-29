# So do tong the theo tung phan - dang flowchart

File nay ve theo kieu so do hop va mui ten nhu hinh mau. Tat ca so do dung Mermaid `flowchart`, xem truc tiep duoc tren GitHub Markdown.

---

## 1. Tong the he thong

```mermaid
flowchart LR
  subgraph shared["auction-shared: contract dung chung"]
    REQ["Request<br/>action, requestId, payload"]
    RES["Response<br/>status, message, payload"]
    MODEL["Model chung<br/>User, Item, BidTransaction, Rating, ChatMessage"]
    ENUM["Enum<br/>UserRole, ItemStatus, AuctionType"]
  end

  subgraph client["auction-client: JavaFX"]
    APP["App.main / Main.start"]
    SCENE["SceneManager<br/>doi man hinh"]
    UI["FXML + Controller<br/>Login, TrangChu, ItemInfo, Chat, Profile"]
    NET["NetworkClient<br/>gui Request qua TCP socket"]
    ROUTER["IncomingResponseRouter<br/>phan loai Response / realtime event"]
  end

  subgraph server["auction-server: backend"]
    MAIN["Main"]
    SOCKET["SocketServer<br/>lang nghe port 8080"]
    HANDLER["ClientHandler<br/>moi socket mot handler"]
    REG["ActionRegistry<br/>map action -> handler"]
    SERVICE["Service layer<br/>AuctionManager, UserService, SettlementService"]
    DAO["DAO layer<br/>UserDao, ItemDao, BidDao, ChatDao"]
  end

  DB[("MySQL<br/>users, items, bids, logs, chat")]

  APP --> SCENE --> UI --> NET
  NET -->|"Request JSON"| HANDLER
  HANDLER -->|"Response JSON"| ROUTER
  ROUTER --> UI
  MAIN --> SOCKET --> HANDLER --> REG --> SERVICE --> DAO --> DB
  client -. uses .-> shared
  server -. uses .-> shared
```

---

## 2. May khach JavaFX `com.auction.client`

```mermaid
flowchart LR
  CS["ClientSession<br/>user hien tai<br/>role dang bat<br/>watchlistIds"]

  APP["Ham App.main"] --> MAIN["Ham Main.start<br/>khoi dong JavaFX"]
  MAIN --> SM["SceneManager<br/>doi Scene"]
  SM --> AUTH["Welcome / Login / Register / ForgotPassword"]
  AUTH --> KC["KhungController<br/>khung chinh canh va tab"]

  KC --> TC["TrangChuController"]
  KC --> WL["WatchlistController"]
  KC --> HIS["HistoryController"]
  KC --> YI["YourItemController"]
  KC --> AD["AdminDashboardController"]
  KC --> CH["ChatPageController"]
  KC --> II["ItemInformationController"]
  KC --> NL["AddNewLotController"]
  KC --> SK["ThanhTimKiemController"]

  II --> BCS["Dich vu<br/>BiddingClientService"]
  NL --> LSS["Dich vu<br/>LotSubmissionService"]
  KC --> PF["ProfileController"]
  PF --> UAS["Dich vu<br/>UserAccountService"]

  subgraph net["Duong di cua tin tu server"]
    NC["NetworkClient<br/>mot instance duy nhat"]
    RX["IncomingResponseRouter<br/>doc loai tin"]
    BRIDGE["MainShellNetworkBridge<br/>trien khai NetworkEventListener"]
    NC --> RX --> BRIDGE
  end

  BCS --> NC
  LSS --> NC
  UAS --> NC
  KC -. doc/ghi .-> CS
  BRIDGE -. cap nhat UI .-> KC
  BRIDGE -. cap nhat ho so .-> PF
```

---

## 3. Server: tu socket den handler

```mermaid
flowchart TB
  subgraph boot["Khoi dong server"]
    MAIN["Main<br/>chay migration DB<br/>gan shutdown hook"]
    SOCKET["SocketServer<br/>mo port 8080<br/>thread pool"]
    SETTLE["SettlementService.start<br/>xu ly phien het han"]
    MAIN --> SOCKET
    MAIN --> SETTLE
  end

  subgraph request["Moi ket noi client"]
    CLIENT["JavaFX Client"]
    CH["ClientHandler<br/>doc length + JSON"]
    TB["TokenBucket<br/>gioi han spam request"]
    JSON["ObjectMapper<br/>Request / Response"]
    CTX["HandlerContext<br/>currentUser + service + DAO"]
    CLIENT --> CH
    CH --> TB
    CH --> JSON
    CH --> CTX
  end

  subgraph dispatch["Dieu phoi action"]
    REG["ActionRegistry"]
    AUTH["ActionHandler.requireAuth"]
    ADMIN["ActionHandler.requireAdmin"]
    HANDLERS["Cac handler<br/>Login, Bid, Chat, Rating, Watchlist, Admin"]
    REG --> AUTH
    REG --> ADMIN
    AUTH --> HANDLERS
    ADMIN --> HANDLERS
  end

  subgraph result["Ket qua tra ve"]
    OK["Response.OK<br/>payload du lieu"]
    ERR["Response.ERROR<br/>message loi"]
  end

  CH --> REG
  HANDLERS --> OK
  HANDLERS --> ERR
  OK --> CLIENT
  ERR --> CLIENT
```

---

## 4. Server: nghiep vu dau gia

```mermaid
flowchart LR
  BIDREQ["Request.BID<br/>payload BidTransaction"] --> BH["BidHandler"]
  BH --> AM["AuctionManager<br/>quan ly logic dau gia"]

  subgraph pipeline["Auction bid pipeline"]
    VALID["BidAuctionValidator<br/>kiem tra gia, item, user, so du"]
    MANUAL["ManualBidExecutor<br/>bid thu cong"]
    AUTO["AutoBidCoordinator<br/>auto-bid maxBid + increment"]
    REG["AutoBidRegistration<br/>thong tin dang ky auto-bid"]
    VALID --> MANUAL
    VALID --> AUTO
    AUTO --> REG
  end

  subgraph state["Cap nhat trang thai"]
    ITEMDAO["ItemDao<br/>currentPrice, status, winner"]
    BIDDAO["BidDao<br/>ghi bid_transactions"]
    WALLET["TransactionLogDao<br/>hold, refund, sold"]
    USERDAO["UserDao<br/>tru/cong balance"]
  end

  subgraph realtime["Thong bao realtime"]
    NOTI["AuctionRealtimeNotifier"]
    HUB["ClientConnectionHub<br/>sendToUser / broadcast"]
    LISTENER["Client NetworkEventListener<br/>cap nhat UI"]
    NOTI --> HUB --> LISTENER
  end

  AM --> VALID
  MANUAL --> ITEMDAO
  MANUAL --> BIDDAO
  AUTO --> ITEMDAO
  AUTO --> BIDDAO
  ITEMDAO --> WALLET
  WALLET --> USERDAO
  AM --> NOTI
```

---

## 5. Server: database va migration

```mermaid
flowchart TB
  START["Server start"] --> MIG["DatabaseMigration.runAll"]

  subgraph migrations["Cac schema migration"]
    USERM["UserSchemaMigration"]
    ITEMM["ItemSchemaMigration"]
    BIDM["BidTransactionsSchemaMigration"]
    LOGM["TransactionLogSchemaMigration"]
    RATINGM["RatingsSchemaMigration"]
    CHATM["ChatSchemaMigration"]
    FRIENDM["FriendshipSchemaMigration"]
  end

  MIG --> USERM
  MIG --> ITEMM
  MIG --> BIDM
  MIG --> LOGM
  MIG --> RATINGM
  MIG --> CHATM
  MIG --> FRIENDM

  subgraph dao["DAO layer"]
    BASE["BaseDao<br/>queryList, querySingle, executeUpdate"]
    USERDAO["UserDao"]
    ITEMDAO["ItemDao"]
    LOTDAO["LotDao"]
    BIDDAO["BidDao"]
    RATINGDAO["RatingDao"]
    LOGDAO["TransactionLogDao"]
    CHATDAO["ChatDao"]
    FRIENDDAO["FriendDao"]
    WATCHDAO["WatchlistDao"]
  end

  CONN["DatabaseConnection<br/>HikariCP pool"]
  DB[("MySQL")]

  USERDAO --> BASE
  ITEMDAO --> BASE
  LOTDAO --> BASE
  BIDDAO --> BASE
  RATINGDAO --> BASE
  LOGDAO --> BASE
  CHATDAO --> BASE
  FRIENDDAO --> BASE
  WATCHDAO --> BASE
  BASE --> CONN --> DB
  migrations --> CONN
```

---

## 6. Chat, friend va realtime notification

```mermaid
flowchart LR
  subgraph client["Client UI"]
    CHATUI["ChatPageController"]
    EXEC["ChatRequestExecutor"]
    BUBBLE["ChatBubbleRowFactory"]
    CELL["ChatLeftListCell"]
    POPUP["NotificationCenter / NotificationPopup"]
  end

  subgraph socket["Socket request / realtime event"]
    NC["NetworkClient"]
    RX["IncomingResponseRouter"]
    LISTENER["NetworkEventListener"]
  end

  subgraph server["Server handler"]
    CHATH["ChatHandler"]
    FRIENDH["FriendHandler"]
    HUB["ClientConnectionHub"]
  end

  subgraph database["DAO"]
    CHATDAO["ChatDao<br/>global/private history"]
    FRIENDDAO["FriendDao<br/>friend request/status"]
  end

  CHATUI --> EXEC --> NC
  CHATUI --> BUBBLE
  CHATUI --> CELL
  NC --> CHATH
  NC --> FRIENDH
  CHATH --> CHATDAO
  FRIENDH --> FRIENDDAO
  CHATH --> HUB
  FRIENDH --> HUB
  HUB --> RX --> LISTENER
  LISTENER --> CHATUI
  LISTENER --> POPUP
```

---

## 7. Map nhanh theo chuc nang bao ve

```mermaid
flowchart LR
  REQ["Yeu cau de bai"] --> USER["Quan ly nguoi dung<br/>LoginHandler, SignupHandler, UserDao"]
  REQ --> ITEM["Quan ly san pham<br/>AddLotHandler, ItemQueryHandler, ItemDao"]
  REQ --> BID["Dau gia<br/>BidHandler, AuctionManager, BidDao"]
  REQ --> END["Ket thuc phien<br/>SettlementService, AuctionEndEvent"]
  REQ --> REAL["Realtime<br/>AuctionRealtimeNotifier, ClientConnectionHub"]
  REQ --> GUI["GUI JavaFX<br/>FXML, Controller, SceneManager"]
  REQ --> TEST["Kiem thu / build<br/>JUnit, Mockito, Maven Shade"]

  USER --> DB[("MySQL")]
  ITEM --> DB
  BID --> DB
  END --> DB
  REAL --> GUI
```

