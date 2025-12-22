# System Design Specification: Mini Restaurant App

> **Version**: 1.1 (Released: 2025-12-21)
> **Status**: Active Development
> **Authors**: Antigravity AI Agent

---

## 1. Executive Summary

The **Mini Restaurant App** is a scalable, cloud-native e-commerce platform designed to demonstrate modern software architecture. It facilitates:
1.  **Customer Operations**: Browsing menus, managing carts, and placing orders.
2.  **Admin Operations**: Order management and Dashboard monitoring.
3.  **Action**: Sends an email/SMS or pushes a WebSocket update to the admin dashboard.

The system is built on a **Microservices** backend (Spring Cloud) and a **Micro-frontend** client (Vue 3 Module Federation), emphasizing separation of concerns, scalability, and security.

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

### 3.4 Admin Order Management Sequence

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

---

## 7. API Catalog (Key Endpoints)

> **API Documentation**: 
> - **Auth Service**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
> - **Order Service**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
> - **OpenAPI Specs**: `/v3/api-docs` on respective ports.

### 7.1 Auth Service (`/api/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/login` | Public | Authenticates user (RSA encrypted password), returns JWT + Roles. |
| `POST` | `/register` | Public | Registers new user (RSA encrypted password). |
| `GET` | `/public-key` | Public | Returns the RSA Public Key for frontend encryption. |
| `GET` | `/verify` | Public | Validates a JWT token. |

### 7.2 Order Service (`/api/orders`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/create` | Authenticated | Creates a new order with items. Publishes event to RabbitMQ. |
| `GET` | `/my` | Authenticated | Retrieves order history for the current logged-in user. |
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
-   **WIP**: Notification Service (Consumer), Menu Management (CRUD).

---
