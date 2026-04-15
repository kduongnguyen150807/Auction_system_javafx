# UML Bản Dễ Đọc (Giảm Rối)

Tài liệu này là phiên bản UML tinh gọn để đọc nhanh.  
Bản đầy đủ vẫn nằm ở: `Summary/project-uml-bản đầy đủ.md`.

---

## Cách đọc đề xuất

1. Xem **Sơ đồ 1** để nắm kiến trúc tổng thể.
2. Xem **Sơ đồ 2** để hiểu luồng request server.
3. Xem **Sơ đồ 3A/3B** để hiểu model dùng chung.
4. Xem **Sơ đồ 4** để hiểu client UI.
5. Xem **Sơ đồ 5** để hiểu realtime.

> Nguyên tắc giảm rối: sơ đồ tổng quan **không nhét tất cả method**.  
> Method chi tiết xem ở file full.

---

## Sơ đồ 1 - Kiến trúc liên module

```mermaid
%%{init: {'flowchart': {'curve': 'linear', 'nodeSpacing': 70, 'rankSpacing': 80}} }%%
flowchart LR
  C[auction-client] <--> SH[auction-shared]
  S[auction-server] <--> SH
  C -->|Socket Request/Response| S
  S --> DB[(MySQL)]
```

---

## Sơ đồ 2 - Server request pipeline

```mermaid
%%{init: {'flowchart': {'curve': 'linear', 'nodeSpacing': 80, 'rankSpacing': 90}} }%%
flowchart TB
  subgraph IN["Tầng vào"]
    SS[SocketServer] --> CH[ClientHandler]
  end

  subgraph SV["Tầng service"]
    US[UserService]
    AM[AuctionManager]
    BS[BidService]
  end

  subgraph DA["Tầng DAO"]
    UDAO[UserDao]
    DAO1[ItemDao / LotDao / RatingDao / TransactionLogDao]
    DAO2[BidDao + ItemDao]
    DBC[DatabaseConnection]
  end

  CH --> US
  CH --> AM
  CH --> DAO1
  AM --> BS
  US --> UDAO
  BS --> DAO2
  UDAO --> DBC
  DAO1 --> DBC
  DAO2 --> DBC
  DBC --> DB[(MySQL)]
```

---

## Sơ đồ 3 - Shared domain model (tách 2 cụm để đỡ chồng)

### Sơ đồ 3A - User hierarchy

```mermaid
%%{init: {'classDiagram': {'nodeSpacing': 80, 'rankSpacing': 90}} }%%
classDiagram
direction LR

class Entity
class User
class Admin
class Seller
class Bidder
class UserRole

Entity <|-- User
User <|-- Admin
User <|-- Seller
User <|-- Bidder
User ..> UserRole
```

### Sơ đồ 3B - Item + protocol core

```mermaid
%%{init: {'classDiagram': {'nodeSpacing': 80, 'rankSpacing': 90}} }%%
classDiagram
direction LR

class Entity
class Item
class Art
class Electronics
class Vehicle
class ItemStatus
class BidTransaction
class Request
class Response
class ItemFactory

Entity <|-- Item
Entity <|-- BidTransaction
Item <|-- Art
Item <|-- Electronics
Item <|-- Vehicle
Item ..> ItemStatus
ItemFactory ..> Item
Request ..> BidTransaction
Response ..> Item
```

---

## Sơ đồ 4 - Client UI orchestration

```mermaid
%%{init: {'flowchart': {'curve': 'linear', 'nodeSpacing': 75, 'rankSpacing': 85}} }%%
flowchart LR
  MAIN[Main + SceneManager] --> AUTH[Welcome / Login / Register]
  AUTH --> KHUNG[KhungController]

  subgraph CORE["Các tab chính"]
    KHUNG --> TC[TrangChu]
    KHUNG --> HI[History]
    KHUNG --> YI[YourItem]
    KHUNG --> PR[Profile]
    KHUNG --> AD[AdminDashboard]
    KHUNG --> II[ItemInformation]
  end

  subgraph DETAIL["Màn hình phụ thuộc"]
    TC --> IC[ItemCard]
    II --> BF[BiddingForm]
    II --> RF[RatingForm]
    KHUNG --> SB[ThanhTimKiem]
    SB --> UP[UserProfile]
    PR --> TH[TransactionHistory]
    KHUNG --> AL[AddNewLot]
  end
```

---

## Sơ đồ 5 - Realtime events

```mermaid
sequenceDiagram
  participant S as AuctionManager/Service
  participant N as NetworkClient
  participant K as KhungController
  participant P as ProfileController
  participant NC as NotificationCenter

  S->>N: BALANCE_UPDATE(User)
  N->>P: updateBalanceDirectly(...)

  S->>N: NEW_BID_UPDATE(Item)
  N->>K: updateRealtimeUi(...)

  S->>N: OUTBID_NOTIFY(itemId)
  N->>NC: addNotification(...)
```

---

## Bảng chỉ mục class theo nhóm (để tìm nhanh)

| Nhóm | Class chính |
|---|---|
| Shared Core | `Entity`, `User`, `Item`, `BidTransaction`, `Request`, `Response` |
| Shared Subtypes | `Admin`, `Seller`, `Bidder`, `Art`, `Electronics`, `Vehicle` |
| Server Entry | `Main`, `SocketServer`, `ClientHandler` |
| Server Service | `AuctionManager`, `BidService`, `SettlementService`, `AuctionCloser`, `UserService` |
| Server DAO | `DatabaseConnection`, `UserDao`, `ItemDao`, `BidDao`, `LotDao`, `RatingDao`, `TransactionLogDao` |
| Client Core | `Main`, `SceneManager`, `ClientSession`, `NetworkClient` |
| Client Controllers | `KhungController`, `TrangChuController`, `ItemInformationController`, `ProfileController`, `AdminDashboardController`, `HistoryController`, `YourItemController`, `AddNewLotController`, `ThanhTimKiemController` |

---

## Khi nào dùng bản nào

- Dùng `project-uml-clean.md`: đọc nhanh, onboarding, review kiến trúc.
- Dùng `project-uml-complete.md`: tra cứu đầy đủ method/visibility.

