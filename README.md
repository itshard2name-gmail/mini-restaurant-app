# Mini Restaurant App (Microservices + Micro-frontends)

A modern, scalable restaurant application built with Spring Cloud (Backend) and Vue 3 + Module Federation (Frontend).

> **📖 Source of Truth**: For detailed architecture, API specs, and technical decisions, please refer to [SYSTEM_DESIGN.md](./SYSTEM_DESIGN.md).

---

## 1. System Overview

Welcome to the Mini Restaurant App! This platform demonstrates a robust, modern microservices architecture tailored for scalable e-commerce scenarios. It features a dual-interface design catering to both customers and administrators, powered by a resilient backend ecosystem.

### 🌟 User-Facing Features

#### 🛍️ Client Application (Customer View)
- **Interactive Menu**: Browse a dynamic list of menu items with real-time availability.
- **Smart Cart**: Add items to your cart, view totals, and manage quantities effortlessly.
- **Secure Checkout**: Seamless order placement process with instant feedback.
- **Order History**: Track your past orders and their processing status (Pending, preparing, etc.).

#### 🛡️ Admin Dashboard (Management View)
- **RBAC Security**: Role-Based Access Control ensuring only authorized personnel access sensitive data.
- **Menu Management**: (Coming soon) Interface to create, update, and delete menu items.
- **Order Monitoring**: (Coming soon) Real-time view of incoming orders to streamline kitchen operations.

#### 📊 Backend Monitoring (DevOps View)
- **Centralized Dashboard**: A powerful **Spring Boot Admin** interface to monitor the health of all microservices.
- **Real-Time Metrics**: Track CPU usage, Memory (Heap/Non-Heap), Thread states, and HTTP request throughput.
- **Service Discovery**: Visual confirmation of all active services (Auth, Order, Gateway, Notification) via Eureka.

---

## 2. Project Structure

- **`infrastructure/`**: Envoy proxy config and DB init scripts.
- **`backend-services/`**: Maven multi-module monorepo.
    - `registry-server`: Eureka Discovery (8761)
    - `admin-server`: **[NEW]** Spring Boot Admin Dashboard (9090)
    - `gateway-service`: Spring Cloud Gateway (8080 internal)
    - `auth-service`: Authentication & JWT (RSA + BCrypt)
    - `order-service`: Order management & RabbitMQ producer
    - `notification-service`: Async notification consumer
- **`frontend/`**:
    - `host-app`: Main Shell (Vite + Vue 3)
    - `sub-app-menu`: Menu & Cart Remote (Mounted at `/`)
    - `sub-app-admin`: Admin Dashboard Remote (Mounted at `/admin`)

## 3. Quick Start

### 3.1 Backend Infrastructure (Docker)
Start the entire backend ecosystem including databases, message queues, and monitoring:

```bash
docker-compose up --build -d
```

**Service Endpoints**:
- **Application Entry (Envoy)**: http://localhost:8080
- **Admin Monitoring Dashboard**: http://localhost:9090 (Login not required for local dev)
- **Eureka Dashboard**: http://localhost:8761
- **MySQL**: Port 3307 (`root`/`root`)

> **Note**: On the first `docker-compose up`, the `auth-service` automatically seeds the database with initial users (`admin`, `customer`) via `import.sql`.

### 3.2 Frontend (Development)
The frontend uses **Vite Plugin Federation**. You need to run the host and all sub-apps simultaneously for full functionality.

```bash
# Terminal 1: Host App
cd frontend/host-app && npm run dev

# Terminal 2: Menu Sub-app
cd frontend/sub-app-menu && npm run preview

# Terminal 3: Admin Sub-app
cd frontend/sub-app-admin && npm run preview
```
> **CRITICAL**: Sub-apps (Menu, Admin) **MUST** be run in `preview` mode (after `npm run build`). Running them in `dev` mode will fail to generate/serve `remoteEntry.js`, preventing the Host App from loading module federation remotes.

Access the app at: **http://localhost:5000**

## 4. Key Technical Highlights

- **Micro-Frontends**: Independent deployment of Menu and Admin interfaces using Module Federation.
- **Security**:
    - **End-to-End Encryption**: RSA-encrypted password transmission during login.
    - **Database-backed Authentication**: MySQL storage with BCrypt hashing for passwords.
    - **JWT & RBAC**: Stateless authentication with strictly enforced Role-Based Access Control on both Frontend (Guards) and Backend (PreAuthorize).
    - **Redis Integration**: High-performance token management and white-listing.
-   **Observability**:
    -   **Actuator & Prometheus**: Full metric exposure (`/actuator/**`) enabled on all services for deep system insights.
    -   **Visual Monitoring**: Integrated Spring Boot Admin for real-time health checks and JVM diagnostics.
- **Resilience**: RabbitMQ for asynchronous, decoupled order processing.
- **Network**: "Defense in Depth" topology with Envoy Proxy and Spring Cloud Gateway.

## 4. Development Guidelines

Please refer to [SYSTEM_DESIGN.md](./SYSTEM_DESIGN.md) for:
- 🛠 **Technology Stack** versions
- 🔐 **Security Architecture** details
- 🧱 **Adding new Microservices/Sub-apps**
- 📡 **API Catalog**

### Security Rules (Strict)
- **Secrets**: Manage local secrets in root `.env` (git-ignored).
- **Logs**: No sensitive PII or tokens in logs.

---
*Maintained by Antigravity AI Agent*
