# System Design Specification: Mini Restaurant App

> **Version**: 2.0 (Released: 2025-12-23)
> **Status**: **Commercial / Production-Ready** (Stable)
> **Author**: Antigravity AI Agent

---

## 1. Executive Summary

**Mini Restaurant App** is a scalable, cloud-native e-commerce platform architected for **real-world commercial operation**. Moving beyond a simple MVP, this system is designed to handle multi-region deployments, timezone complexities, and high-concurrency traffic.

Its features include:
1.  **Customer Operations**: Menu browsing, cart management, ordering, and self-cancellation.
2.  **Admin Operations**: Real-time dashboard, BI reports (Analytics), and menu management with AI integration.
3.  **Global Readiness**: Multi-timezone support, i18n-ready architecture, and robust security standards.
4.  **Guest Access**: Frictionless "Lazy Login" and "Quick Login" (Mobile-First) flows.

Built upon a **Microservices** backend (Spring Cloud) and **Micro-frontend** client (Vue 3 Module Federation), the system emphasizes separation of concerns, scalability, security, and a premium user experience.

---

## 2. High-Level Architecture

### 2.1 Traffic Flow Topology
The network topology implements a "Defense in Depth" strategy with three distinct zones.

```mermaid
graph TD
    %% Frontend Layer
    subgraph Client_Side ["Client Side / Browser"]
        Host["Host App (Shell)"]
        Menu["Sub-app: Menu"]
        Admin["Sub-app: Admin"]
        Host -->|Loads| Menu
        Host -->|Loads| Admin
    end

    %% Edge Layer
    subgraph DMZ ["Public Zone (DMZ)"]
        Envoy["Envoy Proxy :8080"]
    end

    %% Backend Layer
    subgraph Internal_Zone ["Internal Trust Zone"]
        Gateway["Spring Cloud Gateway :8080"]
        Eureka["Eureka Registry :8761"]
        
        subgraph Services ["Microservices"]
            Auth["Auth Service"]
            Order["Order Service"]
            Notif["Notification Service"]
        end
    end

    %% Data Layer
    subgraph Data_Layer ["Data Persistence & Messaging"]
        MySQL[("MySQL :3307")]
        Redis[("Redis")]
        RabbitMQ(("RabbitMQ"))
    end

    %% Connections
    Client_Side -->|"1. HTTPS/WSS"| Envoy
    Envoy -->|"2. Route /auth, /api"| Gateway
    Gateway -->|"3. Load Balance"| Auth
    Gateway -->|"3. Load Balance"| Order
    
    Auth -->|"Read/Write"| MySQL
    Auth -->|"Cache Token"| Redis
    
    Order -->|"Read/Write"| MySQL
    Order -->|"4. Publish Events"| RabbitMQ
    
    RabbitMQ -->|"5. Consume"| Notif
    
    %% Service Discovery
    Services -.->|"Register Heartbeat"| Eureka

    %% Styling
    classDef client fill:#e1f5fe,stroke:#0277bd,stroke-width:2px,color:#000000;
    classDef dmz fill:#fff3e0,stroke:#ef6c00,stroke-width:2px,color:#000000;
    classDef internal fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px,color:#000000;
    classDef data fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px,color:#000000;
    
    class Host,Menu,Admin client;
    class Envoy dmz;
    class Gateway,Eureka,Auth,Order,Notif internal;
    class MySQL,Redis,RabbitMQ data;
```

### 2.2 Component Roles
-   **Envoy Proxy**: Edge entry point, TLS termination (planned).
-   **Spring Cloud Gateway**: Internal routing, JWT validation interceptor, Request logging.
-   **Eureka**: Service Registry for dynamic service discovery.
-   **Microservices**: Domain-specific logic containers (Auth, Order, Notification).

---

## 3. Data & Business Logic

The system uses **MySQL 8.0** for relational data persistence. The schema is distributed across service-specific logical databases (e.g., `auth_db`, `order_db`) but depicted here in a unified view.

### 3.1 Entity Relationship Diagram

```mermaid
erDiagram
    %% Auth Service Context
    USER ||--o{ USER_ROLES : "has"
    ROLE ||--o{ USER_ROLES : "assigned to"

    USER {
        Long id PK
        String username "Unique, Not Null"
        String password "BCrypt Encrypted"
        Boolean enabled
    }

    ROLE {
        Long id PK
        String name "Unique (e.g., ROLE_ADMIN)"
    }

    USER_ROLES {
        Long user_id FK
        Long role_id FK
    }

    %% Order Service Context
    ORDER ||--o{ ORDER_ITEM : "contains"
    MENU ||--o{ ORDER_ITEM : "referenced by"

    ORDER {
        Long id PK
        String user_id "From JWT Subject"
        Decimal total_price
        String status "PENDING, COMPLETED"
        DateTime created_at
    }

    ORDER_ITEM {
        Long id PK
        Long order_id FK
        Long menu_id FK
        String snapshot_name "Preserved Name"
        Decimal snapshot_price "Preserved Price"
        Integer quantity
    }

    MENU {
        Long id PK
        String name
        Decimal price
        String description
        String image_url
        String category "New (e.g. Main, Starter)"
    }
```

### 3.2 Design Patterns
-   **Snapshot Pattern**: `ORDER_ITEM` stores `snapshot_name` and `snapshot_price` at the time of purchase. This prevents historical orders from changing if the `MENU` item is updated later.
-   **Loose Coupling**: `ORDER` table links to `USER` via a string `user_id` (from JWT), not a database Foreign Key. This ensures microservice independence.

### 3.3 Order Lifecycle Sequence

```mermaid
sequenceDiagram
    autonumber
    actor User as User (Customer)
    participant FE as Sub-app-Menu
    participant Envoy as Envoy Proxy
    participant GW as Gateway Service
    participant Auth as Auth Service (JWT)
    participant Order as Order Service
    participant DB as MySQL DB
    participant MQ as RabbitMQ
    participant Notif as Notification Service

    %% Phase 1: Authentication
    note over User, Auth: Prerequisite: User Login (RSA Encrypted Password)
    User->>FE: Enter Credentials
    FE->>FE: Encrypt Password (RSA Public Key)
    FE->>Auth: POST /auth/login {user, enc_pass}
    Auth->>DB: Verify Hash (BCrypt)
    Auth-->>FE: Return JWT Token (in Response Body)

    %% Phase 2: Order Creation
    note over User, Notif: Main Flow: Place Order
    User->>FE: Click "Checkout"
    FE->>Envoy: POST /api/orders
    Note right of FE: Header: Authorization: Bearer {JWT}
    Note right of FE: Header: X-User-Id: {userId}
    
    Envoy->>GW: Forward Request
    GW->>Order: Route to Order Service
    
    rect rgb(30, 30, 30)
        note right of GW: Security Context
        Order->>Order: JwtAuthenticationFilter: Validate Signature
        Order->>Order: Extract Roles & UserID
    end

    Order->>DB: Fetch "Menu" Items
    
    %% Snapshot Logic
    rect rgb(50, 50, 50)
        note right of Order: Data Integrity (Snapshot)
        Order->>Order: Create Order Entity
        loop For each Item
            Order->>Order: Copy Name & Price to OrderItem
        end
        Order->>DB: SAVE Order (Commit)
    end
    
    %% Messaging
    Order->>MQ: Publish to "order.exchange"
    Note right of Order: Routing Key: "order.create"
    Order-->>FE: 200 OK (Order JSON)
    FE-->>User: Show "Order Success"

    %% Phase 3: Notification
    MQ->>Notif: Consume Message
    Notif->>Notif: Log / Push WebSocket
    note right of Notif: "New Order #123 Received"
```

### 3.4 Customer Order Cancellation (New)
To reduce friction, customers can cancel their own orders under strict conditions:
-   **Trigger**: Clicks "Cancel Order" in `MyOrders` UI.
-   **Endpoint**: `PATCH /api/orders/{id}/cancel`.
-   **Condition**: Order Status **MUST** be `PENDING`.
-   **Outcome**:
    1.  Status updates to `CANCELLED`.
    2.  Event `order.cancelled` published to RabbitMQ.
    3.  Admin Dashboard updates via WebSocket (moves to History tab).
    4.  Items are not deducted from inventory (as inventory logic is V2).

### 3.5 Admin Order Management Sequence

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Admin User
    participant FE as Sub-app-Admin
    participant GW as Gateway / Envoy
    participant Auth as Auth Service
    participant Order as Order Service
    participant DB as MySQL DB
    participant MQ as RabbitMQ
    participant Notif as Notification Service

    %% Phase 1: View Active Orders
    note over Admin, Order: Scenario: Admin Views Dashboard
    Admin->>FE: Open Dashboard
    FE->>GW: GET /api/orders/admin/all
    Note right of FE: Header: Authorization: Bearer {JWT}
    
    GW->>Order: Forward Request
    
    rect rgb(30,30,30)
        note right of GW: RBAC Check
        Order->>Order: Validate JWT
        Order->>Order: Check Role == ROLE_ADMIN
        alt is Authorized
             Order->>DB: Fetch All Orders
             Order-->>FE: Return List [Order A, Order B...]
        else is Unauthorized
             Order-->>FE: 403 Forbidden
        end
    end

    %% Phase 2: Update Status
    note over Admin, Notif: Scenario: Admin Accepts Order
    Admin->>FE: Click "Accept Order"
    FE->>GW: PATCH /api/orders/admin/{id}/status {status: PREPARING}
    GW->>Order: Forward Request
    
    Order->>Order: Verify ROLE_ADMIN
    Order->>DB: Update Status -> PREPARING
    
    %% Phase 3: Async Notification
    Order->>MQ: Publish "order.update"
    Note right of Order: Routing Key: "order.create" or "order.update"
    Order-->>FE: 200 OK (Updated Order)
    FE-->>Admin: Show "Order Accepted" Toast

    %% Phase 4: Notify User
    MQ->>Notif: Consume Message
    Notif->>Notif: Push WebSocket Notification
    note right of Notif: "Your order is being prepared!"
```

### 3.5 Admin Dashboard Status Mapping

To streamline kitchen operations, the Admin Dashboard filters orders into tabs based on their lifecycle status.

| Dashboard Tab | Included Status(es) (Backend Enum) | Purpose |
| :--- | :--- | :--- |
| **Active** | `PENDING`, `PAID`, `PREPARING`, `READY` | **Default View**. Shows all actionable orders currently in the pipeline. |
| **Pending** | `PENDING`, `PAID` | New orders waiting for restaurant acceptance. |
| **Kitchen** | `PREPARING` | Orders accepted and currently being cooked/assembled. |
| **Counter** | `READY` | Orders finished and waiting for customer pickup. |
| **History** | `COMPLETED`, `CANCELLED` | Archive of finished transactions. Lazy-loaded for performance. |

---

## 4. Security Architecture

### 4.1 Authentication Pipeline (RSA + BCrypt)
A robust "Encryption in Transit" mechanism is used for login.

1.  **Public Key Fetch**: Client `GET /auth/public-key`.
2.  **Encryption**: Client encrypts password using RSA (`JSEncrypt`).
3.  **Transmission**: Encrypted payload sent to `POST /auth/login`.
4.  **Decryption**: Backend decrypts using stored Private Key.
5.  **Verification**: Decrypted password is verified against `BCrypt` hash in DB.

### 4.2 Authorization (JWT + RBAC)
-   **Token Format**: Standard JWT (HS256).
-   **Claims**: `sub` (username), `roles` (["ROLE_ADMIN", ...]), `exp`.
-   **Enforcement**:
    -   **Gateway**: Validates signature.
    -   **Service Layer**: `@PreAuthorize("hasRole('ADMIN')")` secures specific endpoints.

### 4.3 Global Timezone Strategy (Strict Enforcement)
To support global deployment, we adopt a "UTC Storage, Local Display" strategy with **strict type enforcement**:

1.  **Persistence**: 
    -   **Strict Rule**: Entities **MUST** use `java.time.Instant` for all timestamp fields.
    -   **Forbidden**: `LocalDateTime` is strictly prohibited for persistence.
    -   All timestamps must be stored in **UTC**. 
    -   Reliance on DB server local time is prohibited.

2.  **Business Logic (Analytics)**:
    -   **Operational Reports**: The definition of "Today" depends on the **Restaurant's Timezone**, not the server or user.
    -   *Example*: For a Tokyo restaurant, "Daily Revenue" starts at 00:00 JST (15:00 UTC Previous Day).
    -   *Strategy*: Configurable `RestaurantSettings` entity (Planned for V2).

3.  **Presentation**:
    -   API always returns ISO 8601 UTC format.
    -   Browser converts to local time for display (`new Date().toLocaleString()`).

### 4.4 Deep Dive: Timezone Data Flow (Frontend <-> Backend <-> DB)

A common challenge in distributed systems is ensuring time consistency across different computing environments. We resolved this by standardizing on `Instant` for backend entities instead of `LocalDateTime`.

#### The "LocalDateTime" Pitfall
`LocalDateTime` represents "Wall Clock" time (e.g., "10:00 AM") without any timezone context.
- **Ambiguity**: When the frontend sends `2025-12-25T10:00:00Z` (UTC), parsing it into `LocalDateTime` strips the `Z` (UTC marker).
- **Data Corruption**: If the Database Server is in `UTC+8`, saving "10:00" might be interpreted by the driver as "10:00 Taiwan Time" rather than "10:00 UTC", resulting in an 8-hour offset error.

#### The "Instant" Solution
`Instant` represents a specific, absolute point on the universal timeline.
- **Precision**: It explicitly handles the timestamp as a UTC moment.
- **Safety**: The JDBC Driver, recognizing `Instant`, handles the necessary conversion to the database's native storage format (e.g., `TIMESTAMP`) while preserving the exact moment in time, regardless of the server's local timezone settings.

#### Interaction Diagram

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend (Browser)
    participant JSON as JSON Parser (Jackson)
    participant BE as Backend (Java Code)
    participant JDBC as JDBC Driver
    participant DB as MySQL Database

    Note over FE: User in Taiwan (UTC+8)<br/>Selects Time: 18:00 (6 PM)

    FE->>FE: Convert to ISO String (UTC)<br/>18:00 (TW) -> 10:00 (UTC)<br/>String: "2025-12-25T10:00:00Z"
    FE->>BE: Send API Request: { "date": "2025-12-25T10:00:00Z" }

    rect rgb(60, 20, 20)
    Note right of FE: Before Fix: Using LocalDateTime
    BE->>JSON: Parse JSON
    JSON-->>BE: Result: LocalDateTime<br/>Value: "2025-12-25T10:00:00"<br/>❌ "Z" is lost (Relative Time)
    
    BE->>JDBC: Prepare SQL Insert
    Note right of BE: Ambiguous Semantic:<br/>Backend only provides numeric "10:00"
    
    JDBC->>DB: Execute INSERT
    Note right of DB: 😱 Error Potential:<br/>MySQL receives "10:00".<br/>If DB session is UTC+8,<br/>it stores as "10:00 (TW time)".<br/>(Actual instant shifts to UTC 02:00)
    end

    rect rgb(20, 60, 20)
    Note right of FE: After Fix: Using Instant
    BE->>JSON: Parse JSON
    JSON-->>BE: Result: Instant<br/>Value: "2025-12-25T10:00:00Z"<br/>✅ Locked to UTC absolute moment
    
    BE->>JDBC: Prepare SQL Insert
    Note right of BE: Precise Semantic:<br/>"This is UTC 10:00"
    
    JDBC->>DB: Execute INSERT
    Note right of DB: 😃 Correct Conversion:<br/>JDBC Driver recognizes Instant.<br/>It guarantees the stored time<br/>equals UTC 10:00.
    end

    Note over DB, FE: Read Flow (Instant)
    DB->>JDBC: Read Data (TIMESTAMP)
    JDBC->>BE: Convert to Instant (UTC 10:00Z)
    BE->>FE: Return JSON "2025-12-25T10:00:00Z"
    FE->>FE: Browser converts to Local Time (18:00)
```

---

## 5. Micro-Frontends (Module Federation)

We use **Vite Plugin Federation** to compose the UI at runtime.

| App Name | Type | Mount Point | Responsibility |
| :--- | :--- | :--- | :--- |
| **Host App** | Shell | `/` | Layout, Routing, Auth State (Pinia) |
| **Menu App** | Remote | `/` | Food Menu, Shopping Cart |
| **Admin App** | Remote | `/admin` | Dashboard, Order Management |

**Constraint**: Remote apps must be built and served in `preview` mode (`npm run preview`) to expose `remoteEntry.js` correctly during local development.

---

## 6. Technology Stack

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Backend** | Java 17 | Core Language |
| | Spring Boot 3.2 | Application Framework |
| | Spring Cloud 2023 | Gateway, Eureka, OpenFeign |
| | RabbitMQ | Async Messaging |
| | MySQL 8.0 | Primary Database |
| | Redis | Caching & User Sessions |
| **Frontend** | Vue 3.5 | Composition API |
| | Vite 7.2 | Build Tool & Dev Server |
| | Tailwind CSS 3.4 | Utility-first Styling |
| | Pinia | State Management |

### 6.1 UI Standards
-   **Notifications**: The use of browser native `alert()` or `confirm()` is **strictly prohibited** in production (except for debugging). All user feedback must use the standardized **Toast Notification** component.
-   **Styling**: Follow Tailwind CSS Utility-first principles, but maintain semantic consistency for high-level components (Cards, Buttons).

---

## 7. API Catalog (Key Endpoints)

> **API Documentation (Unified Gateway)**: 
> - **URL**: [http://localhost:8088/webjars/swagger-ui/index.html](http://localhost:8088/webjars/swagger-ui/index.html)
> - **Usage**: Select "Auth Service" or "Order Service" from the top-right explorer logic.
> - **OpenAPI Specs**: `/api/auth/v3/api-docs`, `/api/orders/v3/api-docs` (via Gateway).

### 7.1 Auth Service (`/api/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/login` | Public | Authenticates user (RSA encrypted password), returns JWT + Roles. |
| `POST` | `/quick-login` | Public | **(New)** Auto-registers users by mobile number (Mobile First). |
| `POST` | `/register` | Public | Registers new user (RSA encrypted password). |
| `GET` | `/public-key` | Public | Returns the RSA Public Key for frontend encryption. |
| `GET` | `/verify` | Public | Validates a JWT token. |

### 7.2 Order Service (`/api/orders`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/create` | Authenticated | Creates a new order with items. Publishes event to RabbitMQ. |
| `GET` | `/my` | Authenticated | Retrieves order history for the current logged-in user. |
| `PATCH` | `/{id}/cancel` | Authenticated | **(New)** Cancels an order if status is `PENDING` (User owned). |
| `GET` | `/admin/all` | **Admin** | Retrieves all orders in the system. |
| `PATCH` | `/{id}/status` | **Admin** | Updates order status (e.g., `PAID` -> `PREPARING` -> `COMPLETED`). |

### 7.3 Admin Capabilities
-   **Dashboard**: Accessed via `/admin` (Front) and `/api/orders/admin/all` (Back).
-   **Role Check**: Requires `ROLE_ADMIN` in JWT `roles` list.

### 7.4 Backend Monitoring (Actuator)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/actuator/**` | **Public** | Exposes operational information (Health, Metrics, Env, Loggers) for Spring Boot Admin. |
| `GET` | `/actuator/prometheus` | **Public** | Exposes metrics in Prometheus format for scraping. |

> **Note**: For `auth-service`, `gateway-service`, `order-service`, and `notification-service`, these endpoints are fully exposed (`*`) to enable deep monitoring. In `auth-service`, this is explicitly permitted in `SecurityConfig`.

---

## 8. Deployment & Development

### 8.1 Docker Environment
-   **Start**: `docker-compose up --build -d`
-   **Services**: `mysql`, `redis`, `rabbitmq`, `registry`, `gateway`, `auth`, `order`, `envoy`.
-   **Data Persistence**: Named volume `mysql_data` persists DB state.

### 8.2 Adding New Features
1.  **Backend**: Add module -> Update `pom.xml` -> Register with Eureka.
2.  **Frontend**: Create Vue App -> Configure `vite.config.js` (Federation) -> Update Host Router.

---

## 9. Current Implementation Status (v1.1)

-   **Admin UI**: Complete. card-based layout with high contrast.
-   **Auth**: Complete. MySQL-backed, RSA-secured.
-   **Order**: Core flow complete. RabbitMQ Producer implemented.
-   **Notification**: Service complete (RabbitMQ -> WebSocket).
-   **Menu**: Search & Category Filter complete.
-   **Menu Management**: Admin CRUD UI complete.
-   **User Experience UI**: Deferred Login (Lazy Login) & Quick Login (Mobile First) complete.
-   **WIP**: Inventory V2 (Stock tracking).

---
