# User Manual - Mini Restaurant App

## 1. System Overview (系統概述)

The **Mini Restaurant App** is a next-generation food ordering platform designed to bridge the gap between customers and restaurant management.

### Core Value Proposition
-   **For Customers**: A seamless, modern ordering experience with real-time menu updates and instant feedback.
-   **For Managers**: A powerful administrative dashboard to oversee operations, enforce security, and manage orders efficiently.
-   **For DevOps**: Robust backend monitoring to ensure 99.9% uptime and rapid issue resolution.

---

## 2. Quick Start (快速入門)

### 2.1 Environment Setup
Ensure the system is up and running:
1.  **Backend**: `docker-compose up -d --build` (in project root).
2.  **Frontend**: Run the Host App and Sub-Apps (Menu, Admin) as specified in the README.

### 2.2 Portal Access
| Application | URL | Description |
| :--- | :--- | :--- |
| **Storefront** | [http://localhost:3000](http://localhost:3000) | Main customer interface. |
| **Admin Dashboard** | [http://localhost:3000/admin](http://localhost:3000/admin) | Restricted management area. |
| **System Monitor** | [http://localhost:9090](http://localhost:9090) | Spring Boot Admin (DevOps). |

### 2.3 Login Credentials
The system comes pre-loaded with two distinct accounts:

| Role | Username | Password |
| :--- | :--- | :--- |
| **Customer** | `customer` | `123456` |
| **Administrator** | `admin` | `123456` |

> ![Login Page](manual-screenshots/login_page.png)
> *Secure Login Portal*

---

## 3. Feature Guide (功能詳解)

### 3.1 Customer Journey
**Goal**: Place an order for food items.

1.  **Login**: Use the `customer` account.
2.  **Browse Menu**: View available items.
    > ![Customer Menu](manual-screenshots/customer_menu.png)
    > *Interactive Menu*
3.  **Add to Cart**: Click "Add to Cart" on your desired items.
    > ![Customer Cart](manual-screenshots/customer_cart.png)
    > *Cart Management*
4.  **Checkout**: Review your selections in the cart and submit the order.
5.  **Track Order**: Visit "My Orders" to see status updates.

### 3.2 Administrator Journey
**Goal**: Manage restaurant operations.

1.  **Login**: Use the `admin` account.
2.  **Access Dashboard**: Click the "Order Management (Admin)" link in the Navbar.
    > ![Admin Dashboard](manual-screenshots/admin_dashboard.png)
    > *Admin Control Panel*
3.  **Monitor Orders**: View incoming orders in real-time.

### 3.3 DevOps Capabilities
**Goal**: Ensure system health.

1.  **Access Monitor**: Go to `http://localhost:9090`.
2.  **Health Checks**: Verify all microservices (AUTH, ORDER, GATEWAY) are green (UP).
    > ![Backend Monitoring](manual-screenshots/backend_monitoring.png)
    > *Spring Boot Admin Dashboard*

---

## 4. Permissions & Security (RBAC 權限說明)

The system enforces strict Role-Based Access Control (RBAC) at both the Frontend (UI Guards) and Backend (API PreAuthorize).

| Feature / Resource | ROLE_CUSTOMER | ROLE_ADMIN | Mechanism |
| :--- | :---: | :---: | :--- |
| **View Menu** | ✅ | ✅ | Public API |
| **Add to Cart** | ✅ | ❌ | UI Hide + API Block |
| **Place Order** | ✅ | ❌ | API `@PreAuthorize("hasRole('CUSTOMER')")` |
| **View Own Orders** | ✅ | ❌ | Filtered by User ID |
| **View All Orders** | ❌ | ✅ | UI Guard + API Block |
| **Access Admin Page** | ❌ | ✅ | Vue Router Guard |
| **Manage Menu** | ❌ | ✅ | (Future) API Block |

---

## 5. FAQ (常見問題)

### Q1: I see a white screen when accessing the app.
**A:** This usually means the Micro-Frontend remotes failed to load.
-   Ensure you ran `npm run preview` (NOT `dev`) for the `sub-app-menu` and `sub-app-admin` folders.
-   Check browser console (F12) for "Failed to load resource" errors.

### Q2: "Login Failed" or "Network Error".
**A:** The Backend Gateway is likely down.
-   Run `docker ps` to verify `gateway-service` and `auth-service` are running.
-   Check logs: `docker logs gateway-service`.

### Q3: Why can't I see the "Order Management" link?
**A:** You are likely logged in as a `customer`.
-   Logout and sign in with `admin` / `123456`.
