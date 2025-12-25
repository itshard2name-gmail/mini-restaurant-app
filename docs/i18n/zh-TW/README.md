# Mini Restaurant App (商業營運版)

> **狀態**：生產就緒 (Stable)
> **版本**：2.0

這是一個 **可擴展的雲原生電子商務平台**，專為真實世界的商業營運而架構。超越了簡單的 MVP，本系統作為以下技術的參考實作：
-   **Microservices**: Spring Cloud, Eureka, Gateway, OpenFeign.
-   **Micro-Frontends**: Vue 3 + Module Federation (Host & Remote Apps).
-   **全球化營運**: 多時區支援與 i18n 架構就緒。 建構的現代化、可擴展餐廳應用程式。

> **📖 單一資訊來源 (Source of Truth)**：關於詳細的架構、API 規格與技術決策，請參閱 [SYSTEM_DESIGN.md](./SYSTEM_DESIGN.md)。

---

## 1. 系統概覽 (System Overview)

歡迎來到 Mini Restaurant App！本平台展示了專為可擴展電子商務情境設計的穩健、現代化 Microservices 架構。它具備服務顧客與管理員的雙重介面設計，並由具備彈性的後端生態系統驅動。

### 🌟 使用者面向功能

#### 🛍️ 用戶端應用 (顧客視角)
- **互動式菜單**：瀏覽具備 **分類篩選** (主餐, 前菜) 與 **搜尋** 功能的動態菜單列表。
- **智慧購物車**：輕鬆將品項加入購物車、查看總額並管理數量。
- **安全結帳**：無縫的下單流程，並提供即時回饋。
- **訂單歷史**：追蹤您的歷史訂單及其處理狀態 (Pending, preparing 等)。

> ![Login Page](../../../docs/manual-screenshots/login_page.png)
> *安全登入頁面*

> ![Customer Menu](../../../docs/manual-screenshots/customer_menu.png)
> *整合購物車的互動式菜單*

#### 🛡️ Admin Dashboard (管理視角)
- **RBAC 安全機制**：Role-Based Access Control 確保僅有授權人員能存取敏感資料。
- **菜單管理**：建立、更新與刪除菜單品項 (CRUD) 的完整介面。
- **訂單監控**：即時查看新進訂單，並透過 **狀態分頁** (Pending, Kitchen, Counter) 簡化廚房作業。

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
> ⚠️ **警告**：執行 `docker-compose down -v` 將會 **刪除** 此 Volume 與所有資料。若要停止服務但保留資料，請使用 `docker-compose down` (不帶 `-v`)。

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
1.  **選擇模式**：在歡迎畫面選擇 **外帶 (Takeout)** 或 **內用 (Dine-in)**。
2.  **瀏覽菜單**：查看可用餐點 (內用模式顯示桌號)。
3.  **加入購物車**：將品項加入購物車。
4.  **結帳**：
    -   **外帶**：前往結帳 -> 導向登入 -> **快速登入** (輸入手機號) -> 返回購物車 -> 立即付款。
    -   **內用**：選擇桌號 -> 前往結帳 -> **立即下單** (無需登入)。
5.  **查看歷史** (僅限登入用戶)：透過個人選單前往「我的訂單」。
    -   **取消訂單**：在 `PENDING` (未接單) 狀態下可點擊「取消訂單」。
    -   **追蹤**：觀察狀態更新：`PENDING` -> `PREPARING` -> `READY`。

### 4.3 管理員旅程 (Dashboard)
1.  **登入**：登出並使用 **Administrator** 憑證登入。
2.  **存取 Admin Panel**：點擊 Navbar 中的「訂單管理 (Admin)」連結 (僅對管理員可見)。
3.  **管理訂單**：
    -   查看所有成功訂單的列表。
    -   (未來功能) 更新狀態從 `PENDING` 到 `COMPLETED`。

#### 📋 管理儀表板分頁 (Tabs)
儀表板使用以下邏輯來組織訂單：
- **Active (進行中)**: 所有需要關注的訂單 (`PENDING`, `PAID`, `PREPARING`, `READY`)。
- **Pending (待處理)**: 等待接單的新訂單 (`PENDING`, `PAID`)。
- **Kitchen (廚房)**: 正在準備中的訂單 (`PREPARING`)。
- **Counter (櫃檯)**: 製作完成等待取餐的訂單 (`READY`)。
- **History (歷史紀錄)**: 已完成的訂單 (`COMPLETED`, `CANCELLED`)。

### 4.4 系統監控 (DevOps)
-   **Dashboard**：訪問 `http://localhost:9090` (Spring Boot Admin)。
-   **檢查健康狀態**：確認 `AUTH-SERVICE`, `ORDER-SERVICE` 等皆為 **UP**。
-   **查看 Metrics**：點擊 `ORDER-SERVICE` -> **Insights** -> **Metrics** 以查看即時圖表數據。

### 4.5 API 文件 (開發者專用)
-   **統一入口**：Gateway 將所有 Microservice APIs 整合至單一門戶。
-   **互動式 UI**：使用 **Swagger UI** 直接探索 Endpoints 並測試請求。
-   **網址**：`http://localhost:8088/webjars/swagger-ui/index.html` (請選擇 Service: Auth / Order)

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
    -   **API 文件**：整合 **SpringDoc OpenAPI** 與 Gateway Aggregation 實現集中式 API 探索。
- **韌性 (Resilience)**：RabbitMQ 用於非同步、解耦的訂單處理。
- **網路**：具備 Envoy Proxy 與 Spring Cloud Gateway 的「縱深防禦」拓撲。

## 6. 開發指南 (Development Guidelines)

### 6.1 程式碼規範 (Coding Standards) - 重要
為了確保系統穩定性與資料一致性，以下規則為 **強制性 (MANDATORY)**：

-   **時區處理 (Timezone Handling)**:
    -   **必須 (MUST)**: 所有實體的時間戳記欄位 (e.g., `createdAt`, `updatedAt`) 一律使用 `java.time.Instant`。
    -   **禁止 (FORBIDDEN)**: 嚴格禁止使用 `LocalDateTime` 進行資料持久化，以避免時區偏差錯誤。
    -   **API 標準**: JSON 回應中的所有日期時間欄位必須符合 ISO 8601 UTC 格式 (以 `Z` 結尾)。

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
