# So do tong the theo tung phan - Auction System JavaFX

Tai lieu nay gom cac so do class diagram tong the, chia theo tung phan cua du an de dua vao bao cao hoac thuyet trinh. Cac so do dung cu phap Mermaid `classDiagram`, co the xem truc tiep tren GitHub Markdown.

---

## 1. Tong the 3 module

```mermaid
classDiagram
direction LR

namespace auction_shared {
  class Request
  class Response
  class User
  class Item
  class BidTransaction
  class ChatMessage
  class Rating
}

namespace auction_client {
  class App
  class Main
  class SceneManager
  class NetworkClient
  class IncomingResponseRouter
  class ClientSession
}

namespace auction_server {
  class ServerMain
  class SocketServer
  class ClientHandler
  class ActionRegistry
  class HandlerContext
  class AuctionManager
  class DatabaseConnection
}

App --> Main
Main --> SceneManager
NetworkClient --> Request : send
IncomingResponseRouter --> Response : route
ClientHandler --> Request : read
ClientHandler --> Response : write
ClientHandler --> ActionRegistry : dispatch
ActionRegistry --> HandlerContext
HandlerContext --> AuctionManager
AuctionManager --> DatabaseConnection
auction_client ..> auction_shared : uses shared model
auction_server ..> auction_shared : uses shared model
```

Y nghia: `auction-shared` la contract chung giua client va server. Client chi hien thi giao dien va gui request. Server nhan request, xu ly nghiep vu, truy cap database va tra response.

---

## 2. Shared domain model

```mermaid
classDiagram
direction TB

class Entity {
  #int id
}

class User {
  -String username
  -String password
  -String email
  -double balance
  -UserRole role
  -String sessionToken
}

class Bidder
class Seller
class Admin

class Item {
  -String name
  -String description
  -double startingPrice
  -double currentPrice
  -LocalDateTime startTime
  -LocalDateTime endTime
  -ItemStatus status
  -AuctionType auctionType
}

class Vehicle
class Electronics
class Art

class BidTransaction {
  -int itemId
  -int bidderId
  -double bidAmount
  -LocalDateTime bidTime
  -double maxAutoBid
  -double autoBidIncrement
}

class Rating {
  -int itemId
  -int reviewerId
  -int reviewedUserId
  -int stars
  -String comment
}

class TransactionLog {
  -int userId
  -double amount
  -String type
  -LocalDateTime createdAt
}

class ChatMessage {
  -int senderId
  -int receiverId
  -String message
  -LocalDateTime sentAt
}

class Friendship {
  -int requesterId
  -int addresseeId
  -String status
}

class Request {
  -String requestId
  -String action
  -Object payload
  -LocalDateTime timestamp
}

class Response {
  -String requestId
  -String status
  -String message
  -Object payload
  -LocalDateTime timestamp
}

class ItemFactory {
  +createItem(String category) Item
}

class DutchAuctionPricing {
  +effectivePrice(Item item) double
  +formatCountdown(Duration duration) String
}

class PasswordEncoder {
  +hash(String rawPassword) String
}

Entity <|-- User
User <|-- Bidder
User <|-- Seller
User <|-- Admin
Entity <|-- Item
Item <|-- Vehicle
Item <|-- Electronics
Item <|-- Art
Entity <|-- BidTransaction
Entity <|-- ChatMessage
Entity <|-- Friendship
ItemFactory ..> Item : creates
DutchAuctionPricing ..> Item : calculates
Request ..> User : payload
Request ..> Item : payload
Response ..> User : payload
Response ..> Item : payload
BidTransaction --> Item : itemId
BidTransaction --> User : bidderId
Rating --> User : reviewer/reviewed
Rating --> Item : itemId
TransactionLog --> User : userId
ChatMessage --> User : sender/receiver
Friendship --> User : users
```

Phan nay dap ung yeu cau OOP trong de bai: co ke thua `User`, `Item`, co dong goi du lieu, co factory tao loai item va cac lop tien ich dung chung.

---

## 3. Server request dispatch va handler

```mermaid
classDiagram
direction LR

class Main {
  +main(String[] args)
}

class SocketServer {
  -int port
  -ExecutorService pool
  +startServer()
}

class ClientHandler {
  -Socket socket
  -ActionRegistry registry
  -HandlerContext context
  -TokenBucket bucket
  +run()
  +sendResponse(Response response)
}

class TokenBucket {
  -int tokens
  -int max
  +tryConsume() boolean
}

class ActionRegistry {
  -Map~String, ActionHandler~ handlers
  +register(String action, ActionHandler handler)
  +dispatch(Request request, HandlerContext context) Response
}

class ActionHandler {
  <<interface>>
  +handle(Request request, HandlerContext context) Response
  +requireAuth(ActionHandler inner) ActionHandler
  +requireAdmin(ActionHandler inner) ActionHandler
}

class HandlerContext {
  -UserService userService
  -ItemDao itemDao
  -LotDao lotDao
  -TransactionLogDao logDao
  -RatingDao ratingDao
  -User currentUser
}

class LoginHandler
class SignupHandler
class ReconnectHandler
class ForgotPasswordHandler
class BidHandler
class AddLotHandler
class ItemQueryHandler
class LotQueryHandler
class ChatHandler
class FriendHandler
class RatingHandler
class WatchlistHandler
class UserManagementHandler
class MiscHandler

Main --> SocketServer
SocketServer --> ClientHandler : creates per client
ClientHandler --> TokenBucket : rate limit
ClientHandler --> ActionRegistry : dispatch request
ClientHandler --> HandlerContext : owns
ActionRegistry --> ActionHandler : calls
ActionHandler <|.. LoginHandler
ActionHandler <|.. SignupHandler
ActionHandler <|.. ReconnectHandler
ActionHandler <|.. ForgotPasswordHandler
ActionHandler <|.. BidHandler
ActionHandler <|.. AddLotHandler
ActionHandler <|.. ItemQueryHandler
ActionHandler <|.. LotQueryHandler
ActionHandler <|.. ChatHandler
ActionHandler <|.. FriendHandler
ActionHandler <|.. RatingHandler
ActionHandler <|.. WatchlistHandler
ActionHandler <|.. UserManagementHandler
ActionHandler <|.. MiscHandler
HandlerContext --> UserService
HandlerContext --> ItemDao
HandlerContext --> LotDao
HandlerContext --> TransactionLogDao
HandlerContext --> RatingDao
```

Y nghia: moi request di qua `ClientHandler`, duoc gioi han boi `TokenBucket`, sau do `ActionRegistry` dua ve dung handler. Cac action quan trong duoc boc bang `requireAuth` hoac `requireAdmin`.

---

## 4. Server auction core va realtime

```mermaid
classDiagram
direction TB

class AuctionManager {
  -Map sessions
  -LeaderboardService leaderboardService
  +processBid(BidTransaction bid) Response
  +broadcast(Response response)
  +sendToUser(int userId, Response response)
}

class AuctionBidPipeline {
  +process(BidTransaction bid, Item item, User bidder) Response
}

class BidAuctionValidator {
  +validateBid(Item item, User bidder, double amount) Response
  +error(String message) Response
}

class ManualBidExecutor {
  +executeManualBid(BidTransaction bid) Response
}

class AutoBidCoordinator {
  +register(AutoBidRegistration registration)
  +resolveAutoBid(Item item, BidTransaction bid) Response
}

class AutoBidRegistration {
  -int itemId
  -int bidderId
  -double maxBid
  -double increment
  -LocalDateTime registeredAt
}

class AuctionRealtimeNotifier {
  +notifyBid(Item item, BidTransaction bid)
  +notifyClosed(Item item)
}

class ClientConnectionHub {
  +add(ClientHandler handler)
  +remove(ClientHandler handler)
  +broadcast(Response response)
  +sendToUser(int userId, Response response)
}

class SettlementService {
  +start()
  +schedule(Item item)
  +closeExpiredAuction(AuctionEndEvent event)
}

class AuctionEndEvent {
  -int itemId
  -LocalDateTime endTime
}

class DutchAuctionCatalogSync {
  +syncCurrentPrice(Item item)
}

class BanCascadeService {
  +cascadeLockedUser(int userId)
}

class LeaderboardService {
  +refresh()
  +getLeaderboard()
}

class TrendingLotsFormula {
  +score(Item item) double
}

AuctionManager --> AuctionBidPipeline
AuctionManager --> AuctionRealtimeNotifier
AuctionManager --> ClientConnectionHub
AuctionManager --> LeaderboardService
AuctionBidPipeline --> BidAuctionValidator
AuctionBidPipeline --> ManualBidExecutor
AuctionBidPipeline --> AutoBidCoordinator
AutoBidCoordinator --> AutoBidRegistration
AuctionRealtimeNotifier --> ClientConnectionHub
SettlementService --> AuctionEndEvent
SettlementService --> AuctionRealtimeNotifier
DutchAuctionCatalogSync --> AuctionRealtimeNotifier
BanCascadeService --> AuctionRealtimeNotifier
LeaderboardService --> TrendingLotsFormula
```

Phan nay the hien cac diem ky thuat de bao ve: auto-bid, concurrent bidding, realtime update, settlement khi het gio, leaderboard va trending lots.

---

## 5. Server DAO va database

```mermaid
classDiagram
direction TB

class DatabaseConnection {
  +getConnection() Connection
  +closePool()
}

class DatabaseMigration {
  +runAll()
}

class BaseDao~T~ {
  #getConn() Connection
  #queryList(String sql, Object... params) List~T~
  #querySingle(String sql, Object... params) T
  #executeUpdate(String sql, Object... params) boolean
  #mapRow(ResultSet rs) T
}

class UserRepository {
  <<interface>>
}

class ItemRepository {
  <<interface>>
}

class LotRepository {
  <<interface>>
}

class RatingRepository {
  <<interface>>
}

class UserDao
class ItemDao
class LotDao
class BidDao
class RatingDao
class TransactionLogDao
class ChatDao
class FriendDao
class WatchlistDao

DatabaseMigration --> DatabaseConnection
BaseDao --> DatabaseConnection
BaseDao <|-- UserDao
BaseDao <|-- ItemDao
BaseDao <|-- LotDao
BaseDao <|-- BidDao
BaseDao <|-- RatingDao
BaseDao <|-- TransactionLogDao
BaseDao <|-- ChatDao
BaseDao <|-- FriendDao
BaseDao <|-- WatchlistDao
UserRepository <|.. UserDao
ItemRepository <|.. ItemDao
LotRepository <|.. LotDao
RatingRepository <|.. RatingDao

UserDao --> User : maps
ItemDao --> Item : maps
LotDao --> Item : returns list
BidDao --> BidTransaction : maps
RatingDao --> Rating : maps
TransactionLogDao --> TransactionLog : maps
ChatDao --> ChatMessage : maps
FriendDao --> Friendship : maps
WatchlistDao --> Item : reads watchlist ids
```

Y nghia: chi server duoc truy cap database. `BaseDao` gom logic query chung, cac DAO con map tung bang sang object. Migration dam bao schema duoc tao/cap nhat truoc khi server phuc vu client.

---

## 6. Client JavaFX, network va UI controllers

```mermaid
classDiagram
direction LR

class App {
  +main(String[] args)
}

class Main {
  +start(Stage stage)
}

class SceneManager {
  +switchScene(String fxml)
}

class ClientSession {
  -User currentUser
  -Set~Integer~ watchlistIds
  +setCurrentUser(User user)
  +clear()
}

class NetworkClient {
  -DataOutputStream out
  -ConcurrentHashMap pendingMap
  -List~NetworkEventListener~ listeners
  +getInstance() NetworkClient
  +sendRequestAndWait(Request request) Response
  +addListener(NetworkEventListener listener)
  +removeListener(NetworkEventListener listener)
}

class ObjectSocketConnection {
  +connect(String host, int port) ObjectSocketConnection
  +startReadLoop()
}

class IncomingResponseRouter {
  +dispatch(Response response)
}

class NetworkEventListener {
  <<interface>>
  +onNewBidUpdate(Response response)
  +onItemClosed(Response response)
  +onGlobalChat(Response response)
  +onPrivateChat(Response response)
}

class MainShellNetworkBridge
class KhungController
class TrangChuController
class ItemInformationController
class AddNewLotController
class BiddingFormController
class ChatPageController
class WatchlistController
class HistoryController
class ProfileController
class AdminDashboardController

class BiddingClientService
class LotSubmissionService
class UserAccountService

App --> Main
Main --> SceneManager
SceneManager --> KhungController
NetworkClient --> ObjectSocketConnection
NetworkClient --> IncomingResponseRouter
IncomingResponseRouter --> NetworkEventListener
NetworkEventListener <|.. MainShellNetworkBridge
MainShellNetworkBridge --> KhungController
KhungController --> TrangChuController
KhungController --> ItemInformationController
KhungController --> AddNewLotController
KhungController --> ChatPageController
KhungController --> WatchlistController
KhungController --> HistoryController
KhungController --> ProfileController
KhungController --> AdminDashboardController
ItemInformationController --> BiddingClientService
AddNewLotController --> LotSubmissionService
ProfileController --> UserAccountService
BiddingClientService --> NetworkClient
LotSubmissionService --> NetworkClient
UserAccountService --> NetworkClient
KhungController --> ClientSession
```

Y nghia: UI JavaFX duoc chia theo man hinh/controller. Cac controller khong noi thang socket ma di qua service va `NetworkClient`. Realtime update di qua `IncomingResponseRouter` va cac listener.

---

## 7. Chat, friend va notification

```mermaid
classDiagram
direction LR

class ChatHandler {
  +handle(Request request, HandlerContext context) Response
}

class FriendHandler {
  +handle(Request request, HandlerContext context) Response
}

class ChatDao {
  +sendGlobalMessage(ChatMessage message)
  +getGlobalHistory()
  +getPrivateHistory(int userA, int userB)
  +getContacts(int userId)
}

class FriendDao {
  +addFriend(int requesterId, int addresseeId)
  +acceptFriend(int requestId)
  +declineFriend(int requestId)
  +removeFriend(int userA, int userB)
  +getFriends(int userId)
}

class ChatPageController
class ChatRequestExecutor
class ChatBubbleRowFactory
class ChatLeftListCell
class ChatSidebarTab

class NotificationCenter {
  +show(String message)
}

class NotificationPopup {
  +show()
}

class NetworkClient
class IncomingResponseRouter

ChatHandler --> ChatDao
FriendHandler --> FriendDao
ChatDao --> ChatMessage
FriendDao --> Friendship
ChatPageController --> ChatRequestExecutor
ChatPageController --> ChatBubbleRowFactory
ChatPageController --> ChatLeftListCell
ChatPageController --> ChatSidebarTab
ChatRequestExecutor --> NetworkClient
IncomingResponseRouter --> ChatPageController : realtime chat event
IncomingResponseRouter --> NotificationCenter : notify event
NotificationCenter --> NotificationPopup
```

Y nghia: chat va friend co day du phan server handler/DAO va phan client UI. Tin realtime duoc router ve UI, dong thoi co notification popup de bao su kien.

---

## 8. Tom tat quan he theo barem

| Barem | Lop/chum lop chinh |
| --- | --- |
| Quan ly nguoi dung | `User`, `Bidder`, `Seller`, `Admin`, `UserDao`, `LoginHandler`, `SignupHandler`, `UserManagementHandler` |
| Quan ly san pham | `Item`, `Vehicle`, `Electronics`, `Art`, `ItemFactory`, `ItemDao`, `AddLotHandler`, `ItemQueryHandler` |
| Tham gia dau gia | `BidTransaction`, `BidHandler`, `AuctionManager`, `AuctionBidPipeline`, `BidAuctionValidator` |
| Ket thuc phien | `SettlementService`, `AuctionEndEvent`, `AuctionRealtimeNotifier`, `TransactionLogDao` |
| Concurrent bidding | `AutoBidCoordinator`, `ManualBidExecutor`, `BidDao`, `ItemDao`, locking/transaction trong service va DAO |
| Realtime update | `ClientConnectionHub`, `AuctionRealtimeNotifier`, `NetworkEventListener`, `IncomingResponseRouter` |
| GUI JavaFX | `SceneManager`, `KhungController`, `TrangChuController`, `ItemInformationController`, `ChatPageController` |
| Database va migration | `DatabaseConnection`, `BaseDao`, `DatabaseMigration`, cac lop `*SchemaMigration` |

