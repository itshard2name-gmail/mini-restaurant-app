# Implementation Plan: Order Management & Notification

## 1. Overview
Added "My Orders" functionality and RabbitMQ-based notifications to the system.

## 2. Changes Implemented

### 2.1 Services
*   **Notification Service**: Created a new service `notification-service` to listen for order events.
    *   **Configuration**: `pom.xml`, `application.yml`, `Dockerfile`.
    *   **Listener**: `OrderEventListener` listens to `order.created.queue`.
*   **Order Service**:
    *   **RabbitMQ**: Added `spring-boot-starter-amqp`, `RabbitConfig`.
    *   **Event Publishing**: `createOrder` now publishes JSON events to `order.events`.
    *   **New APIs**:
        *   `PATCH /orders/{id}/pay`: Updates order status to PAID.
*   **Infrastructure**: Updated `docker-compose.yml` to include `rabbitmq` and `notification-service`.

### 2.2 Frontend
*   **Sub-App-Menu**:
    *   **Component**: `src/components/MyOrders.vue` (Order list + Pay button).
    *   **Expose**: Added `./MyOrders` to `vite.config.js`.
*   **Host-App**:
    *   **Routing**: Added `/orders` route mapped to `sub-app-menu/MyOrders`.
    *   **Navigation**: Added "My Orders" link in the top bar.

## 3. How to Run
1.  **Rebuild Backend Services**:
    ```bash
    docker-compose up -d --build
    ```
2.  **Frontend**:
    *   The frontend has been rebuilt. Refresh the browser at `http://localhost:3000`.

## 4. Verification
1.  **Frontend**:
    *   Login -> Click "My Orders".
    *   Should see list (initially empty).
    *   Go to Menu -> Add Items -> Checkout.
    *   Go to "My Orders" -> See new order (PENDING).
    *   Click "Pay Now" -> Status changes to PAID.
2.  **Backend Logs**:
    *   Check `notification-service` logs: `docker logs notification-service`.
    *   Expect: `[通知] 收到新訂單 #ID，已通知餐廳準備。`
