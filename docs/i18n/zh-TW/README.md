# Mini Restaurant App (Microservices + Micro-frontends)

一個基於 Spring Cloud (Backend) 與 Vue 3 + Module Federation (Frontend) 建構的現代化、可擴展餐廳應用程式。

> **📖 單一資訊來源 (Source of Truth)**：關於詳細的架構、API 規格與技術決策，請參閱 [SYSTEM_DESIGN.md](./SYSTEM_DESIGN.md)。

---

## 1. 系統概覽 (System Overview)

歡迎來到 Mini Restaurant App！本平台展示了專為可擴展電子商務情境設計的穩健、現代化 Microservices 架構。它具備服務顧客與管理員的雙重介面設計，並由具備彈性的後端生態系統驅動。

### 🌟 使用者面向功能

#### 🛍️ 用戶端應用 (顧客視角)
- **互動式菜單**：瀏覽具備即時供應狀態的動態菜單列表。
- **智慧購物車**：輕鬆將品項加入購物車、查看總額並管理數量。
- **安全結帳**：無縫的下單流程，並提供即時回饋。
- **訂單歷史**：追蹤您的歷史訂單及其處理狀態 (Pending, preparing 等)。

> ![Login Page](../../../docs/manual-screenshots/login_page.png)
> *安全登入頁面*

> ![Customer Menu](../../../docs/manual-screenshots/customer_menu.png)
> *整合購物車的互動式菜單*

#### 🛡️ Admin Dashboard (管理視角)
- **RBAC 安全機制**：Role-Based Access Control 確保僅有授權人員能存取敏感資料。
- **菜單管理**：(即將推出) 建立、更新與刪除菜單品項的介面。
- **訂單監控**：(即將推出) 即時查看新進訂單以簡化廚房作業。

> ![Admin Dashboard](../../../docs/manual-screenshots/admin_dashboard.png)
> *用於訂單管理的 Admin Dashboard*

#### 📊 後端監控 (DevOps 視角)
- **集中式 Dashboard**：強大的 **Spring Boot Admin** 介面，用於監控所有 Microservices 的健康狀態。
- **即時 Metrics**：追蹤 CPU 使用率、Memory (Heap/Non-Heap)、Thread 狀態與 HTTP 請求吞吐量。
- **Service Discovery**：透過 Eureka 視覺化確認所有活躍服務 (Auth, Order, Gateway, Notification)。

> ![Spring Boot Admin](../../../docs/manual-screenshots/backend_monitoring.png)
> *即時系統監控*

---

## 2. 專案結構 (Project Structure)

- **`infrastructure/`**：Envoy proxy 設定與 DB 初始化腳本。
- **`backend-services/`**：Maven multi-module monorepo。
    - `registry-server`：Eureka Discovery (8761)
    - `admin-server`：**[NEW]** Spring Boot Admin Dashboard (9090)
    - `gateway-service`：Spring Cloud Gateway (8080 internal)
    - `auth-service`：Authentication & JWT (RSA + BCrypt)
    - `order-service`：Order management & RabbitMQ producer
    - `notification-service`：Async notification consumer
- **`frontend/`**：
    - `host-app`：Main Shell (Vite + Vue 3)
    - `sub-app-menu`：Menu & Cart Remote (Mounted at `/`)
    - `sub-app-admin`：Admin Dashboard Remote (Mounted at `/admin`)

## 3. 快速開始 (Quick Start)

### 3.1 後端基礎設施 (Docker)
啟動包含資料庫、訊息佇列與監控在內的完整後端生態系統：

```bash
docker-compose up --build -d
```

**服務端點 (Service Endpoints)**：
- **應用程式入口 (Envoy)**：http://localhost:8080
- **Admin 監控 Dashboard**：http://localhost:9090 (本地開發無需登入)
- **Eureka Dashboard**：http://localhost:8761
- **MySQL**：Port 3307 (`root`/`root`)

> **注意**：在首次執行 `docker-compose up` 時，`auth-service` 會透過 `import.sql` 自動以初始使用者 (`admin`, `customer`) 填充資料庫。
> **持久化**：已設定 Docker Named Volume (`mysql_data`) 以在重啟間持久化資料庫變更。

### 3.2 Frontend (Development)
前端使用 **Vite Plugin Federation**。您需要同時執行 Host 與所有 Sub-apps 以獲得完整功能。

```bash
# Terminal 1: Host App
cd frontend/host-app && npm run dev

# Terminal 2: Menu Sub-app
cd frontend/sub-app-menu && npm run preview

# Terminal 3: Admin Sub-app
cd frontend/sub-app-admin && npm run preview
```
> **關鍵 (CRITICAL)**：Sub-apps (Menu, Admin) **必須** 以 `preview` 模式執行 (在 `npm run build` 之後)。若以 `dev` 模式執行，將無法產生/提供 `remoteEntry.js`，導致 Host App 無法載入 Module Federation Remotes。

存取應用程式：**http://localhost:3000**

## 4. 使用者手冊 (Getting Started)

系統運行後，請按照以下步驟探索功能。

### 4.1 登入憑證
系統附帶預先建立的帳戶 (參見 `auth-service/src/main/resources/import.sql`)。

| Role | Username | Password | Access |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `123456` | 完整存取 Storefront 與 Admin Dashboard。 |
| **Customer** | `customer` | `123456` | 存取 Storefront, Cart, 與 Order History。 |

### 4.2 顧客旅程 (Storefront)
1.  **登入**：前往 `http://localhost:3000`。您將被重新導向至 `/login`。使用 **Customer** 憑證登入。
2.  **瀏覽菜單**：您將看到包含可用食品項目的「菜單」頁面。
3.  **加入購物車**：點擊任何項目的「加入購物車」。將會出現通知確認動作。
4.  **結帳**：
    -   打開購物車 (右上角圖示或手機版的 Bottom Sheet)。
    -   檢視項目與總價。
    -   點擊「結帳」。
5.  **查看歷史**：透過 Navbar 個人檔案選單前往「我的訂單」，查看您的 `PENDING` 訂單。

### 4.3 管理員旅程 (Dashboard)
1.  **登入**：登出並使用 **Administrator** 憑證登入。
2.  **存取 Admin Panel**：點擊 Navbar 中的「訂單管理 (Admin)」連結 (僅對管理員可見)。
3.  **管理訂單**：
    -   查看所有成功訂單的列表。
    -   (未來功能) 更新狀態從 `PENDING` 到 `COMPLETED`。

### 4.4 系統監控 (DevOps)
-   **Dashboard**：訪問 `http://localhost:9090` (Spring Boot Admin)。
-   **檢查健康狀態**：確認 `AUTH-SERVICE`, `ORDER-SERVICE` 等皆為 **UP**。
-   **查看 Metrics**：點擊 `ORDER-SERVICE` -> **Insights** -> **Metrics** 以查看即時圖表數據。

## 5. 核心技術亮點 (Key Technical Highlights)

- **Micro-Frontends**：使用 Module Federation 獨立部署 Menu 與 Admin 介面。
- **Security**：
    - **端對端加密**：登入期間的 RSA 加密密碼傳輸。
    - **資料庫支援的驗證**：使用 BCrypt 雜湊密碼的 MySQL 儲存。
    - **JWT & RBAC**：在前端 (Guards) 與後端 (PreAuthorize) 嚴格執行的 Role-Based Access Control 無狀態驗證。
    - **Redis 整合**：高效能的 Token 管理與白名單機制。
-   **可觀測性 (Observability)**：
    -   **Actuator & Prometheus**：所有服務皆啟用完整的 Metric 暴露 (`/actuator/**`) 以進行深度系統洞察。
    -   **視覺化監控**：整合 Spring Boot Admin 以進行即時健康檢查與 JVM 診斷。
- **韌性 (Resilience)**：RabbitMQ 用於非同步、解耦的訂單處理。
- **網路**：具備 Envoy Proxy 與 Spring Cloud Gateway 的「縱深防禦」拓撲。

## 6. 開發指南 (Development Guidelines)

請參閱 [SYSTEM_DESIGN.md](./SYSTEM_DESIGN.md) 以獲取：
- 🛠 **技術棧 (Technology Stack)** 版本
- 🔐 **安全架構** 細節
- 🧱 **新增 Microservices/Sub-apps**
- 📡 **API 目錄**

### 安全規則 (嚴格)
- **Secrets**：在 root `.env` (git-ignored) 中管理本地機密。
- **Logs**：日誌中不得包含敏感 PII 或 Tokens。

---
*由 Antigravity AI Agent 維護*
