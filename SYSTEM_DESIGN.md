# System Design Specification: Mini Restaurant App

## 1. System Architecture Overview
This system adopts a modern **Microservices** and **Micro-frontends** architecture, separating concerns between a secure backend service mesh and a composed frontend interface.

### 1.1 Traffic Flow & Network Topology
The backend architecture is designed with a "Defense in Depth" strategy:

1.  **Public Zone (DMZ)**:
    -   **Envoy Proxy**: Acts as the single entry point (Edge Proxy) for all external traffic. It handles TLS termination (planned) and initial routing.
    -   *Port*: Exposed on `8080`.

2.  **Internal Zone (Trust Zone)**:
    -   **Spring Cloud Gateway**: The internal API Gateway. It performs:
        -   **Route Dispatching**: `/auth/**` -> Auth Service, `/orders/**` -> Order Service.
        -   **Cross-Cutting Concerns**: Request logging, correlation ID injection (future), and global error handling.
    -   **Service Discovery (Eureka)**: All internal microservices satisfy the Client-Side Discovery pattern by registering with Eureka.

3.  **Application Zone**:
    -   **Microservices**: (Auth, Order, Notification) running in isolated containers, communicating via REST (Store-and-Forward) and RabbitMQ (Fire-and-Forget).

**Flow Diagram**:
`User (Browser)` -> `Envoy (:8080)` -> `Spring Cloud Gateway (:8080 internal)` -> `Microservices`

### 1.2 Micro-frontend Architecture
The frontend utilizes **Module Federation** to stitch together independent applications at runtime:

-   **Host App (Shell)**:
    -   **Role**: The main container application. Handles global layout (Navbar), Authentication state (Pinia), and Routing.
    -   **Tech**: Vue 3 + Vite.
    -   **Responsibility**: Loads sub-apps dynamically based on routes (`/`, `/admin`).
    -   **Features**: Conditional Navigation (Admin link visible only to `ROLE_ADMIN`).
-   **Sub-apps (Remotes)**:
    -   **Sub-app-menu**: Displays the food menu and cart. Mounted at `/`.
    -   **Sub-app-admin**: Provides the order management dashboard. Mounted at `/admin`.
    -   **Isolation**: Each sub-app builds independently and exposes components (e.g., `MenuApp`, `AdminDashboard`) to the Host.

---

## 2. Technology Stack

### 2.1 Backend (Java Ecosystem)
-   **Language**: Java 17 (LTS)
-   **Framework**: Spring Boot 3.2.0
-   **Cloud Native**: Spring Cloud 2023.0.0 (Gateway, Eureka)
-   **Build Tool**: Maven (Multi-module project)
-   **Database**: MySQL 8.0 (Relational Data), Redis (Cache/Session)
-   **Messaging**: RabbitMQ (AMQP)
-   **Security**: Spring Security 6, JJWT, Bouncy Castle (Encryption)

### 2.2 Frontend (Modern Web)
-   **Framework**: Vue 3.5.24 (Composition API, Script Setup)
-   **Build Tool**: Vite 7.2.4
-   **Federation**: `@originjs/vite-plugin-federation` v1.4.1
-   **Styling**: Tailwind CSS 3.4.17
-   **State Management**: Pinia
-   **HTTP Client**: Axios

### 2.3 Module Federation Constraints
-   **Local Development**: Vite's dev server (`npm run dev`) does **not** emit `remoteEntry.js` by default.
-   **Requirement**: All Remote Apps (Sub-apps) **MUST** be built and served via `npm run preview` to be consumable by the Host App. Host App can still use `npm run dev`.

---

## 3. Security Architecture

### 3.1 Encryption Pipeline (RSA + BCrypt)
To protect sensitive credentials during transit and at rest:
1.  **Frontend**:
    -   On Login/Register, the client fetches the **Public Key** via `/auth/public-key`.
    -   The password is encrypted using `JSEncrypt` (RSA) before being sent in the JSON payload.
2.  **Backend**:
    -   **Decryption**: The Auth Service uses its **Private Key** (stored in `private_key.pem`) to decrypt the payload.
    -   **Hashing**: The raw password is then hashed using `BCrypt` before comparison or storage in the database.

### 3.2 Authentication & Authorization (JWT + RBAC)
-   **JWT Structure**:
    -   **Header**: `ALGORITHM: HS256`
    -   **Payload**: `sub: username`, `roles: ["ROLE_USER", "ROLE_ADMIN"]`, `exp: timestamp`
    -   **Signature**: HMAC-SHA256 purely used for integrity verification.
-   **Flow**:
    1.  User logs in -> Auth Service validates -> Generates JWT.
        *   *Implementation Note*: Currently, the login flow supports a list of roles, which are embedded in the token claims.
    2.  JWT is returned to the client and stored in `localStorage`.
    3.  Client attaches `Authorization: Bearer <token>` to all subsequent requests.
    4.  **Gateway/Service**: A `JwtAuthenticationFilter` intercepts requests, parses the token, extracts `roles`, and establishes the Security Context.
-   **Redis Token Management**:
    -   Tokens are stored in Redis with a TTL matching the creation expiration (allowlist approach/session tracking).

### 3.3 Configuration Security
-   **Environment Variables**: Sensitive data (Database URLs, JWT Secrets) are **never hardcoded**. They are injected via `docker-compose.yml` using `.env` files.
    -   Example: `JWT_SECRET=${JWT_SECRET}` passed to `auth-service` and `order-service`.

---

## 4. Business Logic & Messaging

### 4.1 Order Snapshot Pattern
To ensure data integrity of historical orders:
-   **Problem**: If a product price changes in the `Menu` table, historical orders should not change value.
-   **Solution**: The `OrderItem` entity stores a **Snapshot** of the product data at the time of purchase.
    -   Fields: `snapshotName`, `snapshotPrice`.
    -   This decouples the `Order` history from current `Product` state.

### 4.2 Asynchronous Messaging (RabbitMQ)
-   **Exchange**: `order.exchange` (Topic Exchange)
-   **Routing Key**: `order.create`
-   **Flow**:
    1.  **Producer (Order Service)**: When an order is saved (`status: PENDING`), a message `{ orderId: 123, status: "PENDING" }` is published.
    2.  **Consumer (Future Notification Service)**: Listens to the queue bound to `order.create`.
    3.  **Action**: Sends an email/SMS or pushes a WebSocket update to the admin dashboard.

---

## 5. Development & Deployment

### 5.1 One-Click Environment (Docker)
The entire stack is containerized.
-   **Start**: `docker-compose up --build -d`
-   **Stop**: `docker-compose down`
-   **Dependency Order**:
    1.  `mysql`, `redis`, `rabbitmq`, `discovery-server` (Base Infra)
    2.  `auth-service`, `order-service`, `gateway-service` (Depend on Infra)
    3.  `envoy` (Depends on Gateway)

### 5.2 Adding a New Microservice
1.  Add module in `backend-services/pom.xml`.
2.  Add `spring-cloud-starter-netflix-eureka-client` dependency.
3.  Annotate Main class with `@EnableDiscoveryClient`.
4.  Add entry in `gateway-service` `application.yml` routes.
5.  Add service to `docker-compose.yml`.

### 5.3 Adding a New Micro-frontend
1.  Create Vue app using `npm create vite@latest`.
2.  Configure `vite.config.js` with `federation` plugin:
    -   `name`: Unique remote name.
    -   `exposes`: List components to share.
3.  In **Host App** `vite.config.js`:
    -   Add remote entry to `remotes` object.
    -   Define a Route in `router/index.js` to load the remote component.

---

## 6. API Catalog

### 6.1 Auth Service (`/api/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/login` | Public | Authenticates user (RSA encrypted password), returns JWT + Roles. |
| `GET` | `/publicKey` | Public | Returns the RSA Public Key for frontend encryption. |

### 6.2 Order Service (`/api/orders`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/create` | Authenticated | Creates a new order with items. Publishes event to RabbitMQ. |
| `GET` | `/my` | Authenticated | Retrieves order history for the current logged-in user. |
| `GET` | `/admin/all` | **Admin** | Retrieves all orders in the system. |
| `PATCH` | `/{id}/status` | **Admin** | Updates order status (e.g., `PAID` -> `PREPARING` -> `COMPLETED`). |

### 6.3 Admin Capabilities
-   **Dashboard**: Accessed via `/admin` (Front) and `/api/orders/admin/all` (Back).
-   **Role Check**: Requires `ROLE_ADMIN` in JWT `roles` list.

### 6.4 Backend Monitoring (Actuator)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/actuator/**` | **Public** | Exposes operational information (Health, Metrics, Env, Loggers) for Spring Boot Admin. |
| `GET` | `/actuator/prometheus` | **Public** | Exposes metrics in Prometheus format for scraping. |

> **Note**: For `auth-service`, `gateway-service`, `order-service`, and `notification-service`, these endpoints are fully exposed (`*`) to enable deep monitoring. In `auth-service`, this is explicitly permitted in `SecurityConfig`.

---

## 7. Release v1.0.0 Status

***(Released: 2025-12-21)***

This section summarizes the concrete implementation details of the first major release.

### 7.1 Completed Features
1.  **Admin Dashboard (UI/UX)**:
    -   **Sub-app**: `sub-app-admin` is fully integrated.
    -   **Design**: Implements a strict "Card Layout" with high contrast (dark text on white cards, gray page background) and padded visual hierarchy.
    -   **Elements**: Includes visual status badges, quantity pills, and clear separation of layout zones.
    -   **Fixes**: Solved "white-on-white" text visibility issues by forcing inline styles (`color: #111827`) to override global defaults.

2.  **Role-Based Access Control (RBAC)**:
    -   **Backend**: `order-service` endpoints (`/admin/all`, `/{id}/status`) are secured with `@PreAuthorize("hasRole('ADMIN')")`.
    -   **Frontend**: The Host App conditionally renders the "Order Management (Admin)" link based on the JWT `roles` claim.

3.  **Database Authentication (Phase 3)**:
    -   **Auth Service**: Fully migrated to **MySQL** backend with `BCrypt` password hashing.
    -   **Seeding**: Automatic data seeding via `data.sql` (packaged in JAR) creates initial `admin` and `customer` users on every startup (idempotent validation).
    -   **Security**: `SecurityConfig` enforces strict access (only login/publicKey public).

4.  **Messaging Infrastructure**:
    -   RabbitMQ dependencies (`spring-boot-starter-amqp`) are added to `order-service`.
    -   Producer logic is in place to publish events on order creation.
    -   *Pending*: The Consumer side (e.g., `notification-service`) is not yet fully implemented.

### 7.2 Configuration & Deviations
-   **Frontend Styling**: The Admin Dashboard uses inline styles in some places to guarantee contrast correctness against the Host App's CSS bleeding. Future refactoring should move these to a scoped Tailwind config or a dedicated theme file.

### 7.3 Next Steps (Recommended)
1.  Implement the RabbitMQ Consumer in `notification-service`.
2.  Refactor `sub-app-admin` CSS to remove inline styles in favor of a robust design system.

---

## 8. Frontend Development Standards

To ensure UI consistency and code quality, all frontend modules (Host and Sub-apps) must adhere to the following standards.

### 8.1 UI Library & Design System
-   **Core Library**: **`shadcn-vue`** (Community-led component library).
-   **Styling**: **Tailwind CSS**.
-   **Installation**:
    1.  Ensure `jsconfig.json` is configured with `"@/*": ["./src/*"]`.
    2.  Update `vite.config.js` to resolve `@` alias.
    3.  Run initialization: `npx shadcn-vue@latest init`.
    4.  Select `New York` style and `Neutral` base color.

### 8.2 Standard Components
Reuse the following components from `@/components/ui` instead of writing raw HTML/CSS:
-   **Interaction**: `Button` (use variants for hierarchy), `Sheet` (for mobile drawers).
-   **Display**: `Card` (content containers), `Badge` (status/price), `AspectRatio` (images).
-   **Layout**: `ScrollArea` (lists inside cards), `Separator`.

### 8.3 Layout & Responsiveness
-   **Desktop (lg+)**:
    -   Adopt a **Split Layout**: Main Content (70%) + Sticky Sidebar (30%).
    -   Use `grid-cols-3` for item listings.
-   **Mobile**:
    -   Single column layout.
    -   Off-canvas elements (like Shopping Cart) must use **Sheet** (Bottom or Side).
    -   Trigger actions via Floating Action Button (FAB) or accessible bottom bar.
-   **Aspect Ratios**: All media (food images) must be wrapped in `AspectRatio` (16:9) to prevent layout shifts.

### 8.4 Theme & Branding
-   **Primary Color**: **Warm Orange-Red** (Food appetite appeal).
    -   HSL Value: `12 90% 55%`
    -   Update `src/style.css` `:root` variables:
        ```css
        --primary: 12 90% 55%;
        --primary-foreground: 0 0% 100%;
        ```
-   **Visual Polish**:
    -   Cards must have `hover:shadow-lg` and `transition-all` classes.
    -   Buttons should support `active:scale-95` for tactile feedback.

### 8.5 Checklist for New Modules
When adding a new frontend sub-app:
1.  [ ] Initialize `shadcn-vue`.
2.  [ ] Copy `src/style.css` theme variables from `sub-app-menu`.
3.  [ ] Install base components: `button`, `card`, `sheet`, `scroll-area`, `separator`, `badge`, `aspect-ratio`.
4.  [ ] Verify responsive behavior (Desktop Sticky vs Mobile Sheet).

---

## 9. Data Persistence & Reliability

### 9.1 Database Persistence Strategy
To prevent data loss during container restarts (`docker-compose down`), the system uses a **Docker Named Volume**:
-   **Volume Name**: `mysql_data`
-   **Mount Path**: `/var/lib/mysql`
-   **Behavior**: When containers are removed, the database files persist in the Docker volume. This ensures that subsequent restarts do not require re-seeding the database, significantly improving startup time and retaining user data.
-   **Reset**: To explicitly wipe only the database data, run `docker volume rm minirestatuantapp_mysql_data`.

### 9.2 SRE & Disaster Recovery Notes
-   **Data vs. Configuration**:
    -   **Data (Recoverable)**: MySQL, Redis, RabbitMQ data. If lost, the system can auto-initialize (via `data.sql` inside the container) to a fresh state. We accept data loss in favor of system recoverability.
    -   **Configuration (Critical)**: Files like `infrastructure/envoy/envoy.yaml`. These are **Single Points of Failure**. If these source files are deleted from the host, containers **will fail to start**.
-   **Recommendation**: maintain strict version control (Git) for all `infrastructure/` configuration files to ensure rapid recovery (Git Pull) in case of accidental deletion.

---
*Generated by Antigravity AI Agent*

## 10. Visual Architecture Diagram

```mermaid
graph TD
    %% Frontend Layer
    subgraph Client_Side [Client Side / Browser]
        Host[Host App (Shell)]
        Menu[Sub-app: Menu]
        Admin[Sub-app: Admin]
        Host -->|Loads| Menu
        Host -->|Loads| Admin
    end

    %% Edge Layer
    subgraph DMZ [Public Zone]
        Envoy[Envoy Proxy :8080]
    end

    %% Backend Layer
    subgraph Internal_Zone [Internal Trust Zone]
        Gateway[Spring Cloud Gateway :8080]
        Eureka[Eureka Registry :8761]
        
        subgraph Services [Microservices]
            Auth[Auth Service]
            Order[Order Service]
            Notif[Notification Service]
        end
    end

    %% Data Layer
    subgraph Data_Layer [Data Persistence & Messaging]
        MySQL[(MySQL :3307)]
        Redis[(Redis)]
        RabbitMQ((RabbitMQ))
    end

    %% Connections
    Client_Side -->|HTTPS/WSS| Envoy
    Envoy -->|Route /auth, /orders| Gateway
    Gateway -->|LB| Auth
    Gateway -->|LB| Order
    
    Auth -->|Read/Write| MySQL
    Auth -->|Cache| Redis
    
    Order -->|Read/Write| MySQL
    Order -->|Publish Events| RabbitMQ
    
    RabbitMQ -->|Consume| Notif
    
    %% Service Discovery
    Services -.->|Register| Eureka

    %% Styling
    classDef client fill:#e1f5fe,stroke:#01579b,stroke-width:2px;
    classDef dmz fill:#fff3e0,stroke:#e65100,stroke-width:2px;
    classDef internal fill:#f3e5f5,stroke:#4a148c,stroke-width:2px;
    classDef data fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px;
    
    class Host,Menu,Admin client;
    class Envoy dmz;
    class Gateway,Eureka,Auth,Order,Notif internal;
    class MySQL,Redis,RabbitMQ data;
```
