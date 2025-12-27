# Documentation & System Sync

- [x] **System Design Documentation**
    - [x] Create `SYSTEM_DESIGN.md` (English)
    - [x] Add Mermaid Traffic Flow Diagram
    - [x] Add Mermaid Entity Relationship Diagram (ERD)
    - [x] Add detailed API Catalog tables
    - [x] **Sequence Diagram**: Add 'Order Lifecycle Sequence' (System Design v1.1)

- [x] **Internationalization (i18n)**
    - [x] Translate `SYSTEM_DESIGN.md` to Traditional Chinese (`docs/i18n/zh-TW/`)
    - [x] Translate `README.md` to Traditional Chinese (`docs/i18n/zh-TW/`)
    - [x] **Sync Rule**: Ensure any update to English docs is immediately synchronized to Chinese docs.

- [ ] **Maintenance**
    - [ ] Monitor `SYSTEM_DESIGN.md` for future architecture changes and sync translation.
    - [ ] Monitor `README.md` for future setup guide changes and sync translation.

# API Documentation & Gateway Optimization

- [x] **Phase 1: Fix Service API Docs (Direct Access)**
    - [x] Fix `OrderController` duplicate mapping causing startup crash.
    - [x] Fix `AuthService` SecurityConfig blocking Swagger UI (403).
    - [x] Verify `http://localhost:8081/swagger-ui.html` and `http://localhost:8082/swagger-ui.html` represent active running services.

- [x] **Phase 2: Gateway Unification (Access Control)**
    - [x] Configure `gateway-service` to route external Swagger requests to internal services.
    - [x] Remove `ports` (8081/8082) mapping from `docker-compose.yml` (Security Hardening).
    - [x] Update `SYSTEM_DESIGN.md` (EN/ZH) with new Gateway-based documentation URLs.
    - [x] Verify external direct access is blocked and Gateway access works.

- [x] **Documentation Updates**
    - [x] Update `README.md` (EN/ZH) with API Documentation feature highlights.

# Cart & Order Optimizations

- [x] **Phase 3: Cart Item Notes**
    - [x] Backend: Add `notes` field to `OrderItem` entity and DTOs.
    - [x] Backend: Update `OrderService` to map notes from request.
    - [x] Frontend (Menu): Add text area in Cart UI and include in order payload.
    - [x] Frontend (Customer): Display notes in My Orders history.
    - [x] Frontend (Admin): Display notes in Order Management details.
    - [x] Verification: Verify complete flow from Cart -> Order -> Admin View.
    
- [x] **Phase 4: Refined Order Workflow ("Ready" State)**
    - [x] **Backend (`OrderService`)**
        - [x] Verify/Add `READY` enum to `OrderStatus` (String based).
        - [x] Ensure state transitions: `PREPARING` -> `READY` -> `COMPLETED` (Fixed in AdminOrderController).
    - [x] **Frontend (Admin)**
        - [x] Update `OrderManagement.vue` to show `READY` button.
        - [x] Add visual indicator (Yellow/Orange) for `READY` state.
    - [x] **Frontend (Customer)**
        - [x] Update `MyOrders.vue` to show "Ready for Pickup" status.

- [x] **Phase 5: Customer WebSocket Updates**
    - [x] **Backend (`OrderEventListener`)**
        - [x] Inject `SimpMessagingTemplate`.
        - [x] Broadcast status updates to `/topic/orders/user/{userId}`.
    - [x] **Frontend (Customer)**
        - [x] Install `sockjs-client` and `@stomp/stompjs`.
        - [x] Implement WebSocket connection in `MyOrders.vue`.
        - [x] Verify real-time updates without polling.

    - [x] **Frontend (Cart.vue)**
        - [x] Implement `router.push('/my-orders')` on success.

- [x] **Phase 7: Order History Optimization**
    - [x] **Backend Implementation**
    - [x] Modify `OrderRepository` to support `Pageable` and searching by `status`, `date`, and `query` (Order ID/User ID).
    - [x] Update `OrderService` to handle `Pageable` and parameters.
    - [x] Create/Update `AdminOrderController` endpoint `/search` to accept pagination and filter params.
- [x] **Frontend Integration**
    - [x] Update `OrderManagement.vue` `fetchOrders` to use the new `/search` endpoint.
    - [x] Add search input (debounced) and date picker to UI.
    - [x] Implement "Previous" / "Next" pagination controls.
    - [x] Update "Active" tab logic to handle server-side filtering constraints.
- [x] **Verification**
    - [x] Verify search by Order ID / User ID.
    - [x] Verify date filtering.
    - [x] Verify pagination works correctly.

- [x] **Phase 8: Admin Dashboard Optimization**
    - [x] **Backend (Order Service)**
        - [x] Update `getAllOrders` to sort by `createdAt DESC`.
    - [x] **Frontend (OrderManagement.vue)**
        - [x] Implement Status Tabs (Active, Pending, Kitchen, Counter, History).
        - [x] Default view to Active.
        - [x] Implement filtering logic.

- [x] **Phase 9: Customer Order Cancellation**
    - [x] **Backend (Order Service)**
        - [x] Add `cancelOrder` method (Restrict to PENDING status).
        - [x] Add endpoint `PATCH /api/orders/{id}/cancel`.
    - [x] **Frontend (MyOrders.vue)**
        - [x] Add "Cancel Order" button for PENDING orders.
        - [x] Handle UI update after cancellation.

- [x] **Phase 10: Menu Search & Filter**
    - [x] **Backend**: Add `category` field to Menu entity.
    - [x] **Frontend**: Implement Search Bar & Category Filter in `MenuList.vue`.

- [x] **Phase 11: Menu Management Polish (Admin)**
    - [x] **Frontend**: Complete CRUD UI in `MenuManagement.vue`.
    - [x] **Frontend**: Add `Category` dropdown.
    - [x] **Frontend**: Add "Magic Fill" (Pollinations.ai) for images.

# User Experience Optimization

- [x] **Phase 12: Deferred Login Flow (Lazy Login)**
    - [x] **Frontend (Host)**: Open `/menu` route to guests.
    - [x] **Frontend (Host)**: Add "Sign In" button to Navbar.
    - [x] **Frontend (Host)**: Implement redirect logic in `Login.vue`.
    - [x] **Frontend (Menu)**: Enforce login only at Checkout in `MenuList.vue`.

- [x] **Phase 13: Quick Login (Mobile First Identity)**
    - [x] **Backend**: Implement `/auth/quick-login` (Auto-register).
    - [x] **Frontend**: Implement Tabs (Customer vs Staff) in `Login.vue`.
    - [x] **Frontend**: Implement Quick Login logic.

- [x] **Phase 14: Analytics Dashboard (Business Intelligence)**
    - [x] **Backend**: Implement `/analytics` endpoints (Summary, Trends, Top Items).
    - [x] **Frontend**: Implement `AnalyticsBoard.vue` with Charts.
    - [x] **Integration**: Add "Reports" tab to Admin Dashboard.
    - [x] **Polish**: Implement "Vibrant Command Center" UI design.

# Globalization Architecture

- [x] **Phase 15: Global Timezone Support**
    - [x] **Backend**: Create `RestaurantSettings` entity (Key-Value store).
    - [x] **Backend**: Seed default timezone (`Asia/Taipei`).
    - [x] **Backend**: Update `AnalyticsController` to use dynamic timezone for calculations.
    - [x] **Frontend**: Add "Settings" UI for Timezone configuration.
    - [x] **Standard**: Establish UI Standards (Toast Notifications).

# Strategic Evolution

- [x] **Strategic Pivot: Production Readiness**
    - [x] **System Design**: Updated Status to "Commercial / Production-Ready".
    - [x] **Constitution**: Established "Toast Notification Only" rule in `SYSTEM_DESIGN.md`.
    - [x] **Architecture**: Mandated Global Timezone support as a core requirement.

# Operational Expansion

- [x] **Phase 16: Dine-In vs Takeout Support**
    - [x] **Product Specs**: Analyze requirements for "Order Types".
    - [x] **Backend**: Add `orderType` (ENUM) and `tableNumber` to Order entity.
    - [x] **Backend**: Update `data.sql` with default `TABLE_LIST`.
    - [x] **Frontend (Menu)**: Add `DiningModeDialog.vue` and Cart logic.
    - [x] **Frontend (Admin)**: Add `TABLE_LIST` setting in SettingsBoard.
    - [x] **Frontend (Admin)**: Update Order Cards with Badges.
    - [x] **Verification**: Manual Testing (User Request) - Validated Fix for Table Sync.

- [x] **Phase 17: Customer UI Polish (Dining Mode Visibility)**
    - [x] **Cart.vue**: Add Badge (Blue/Green) to Cart Header.
    - [x] **MyOrders.vue**: Add Badge to Order History cards.
    - [x] **Verification**: Validated via Manual Testing.

- [x] **Phase 18: Analytics Expansion (Dine-In vs Takeout)**
    - [x] **Backend**: Implement `/api/analytics/distribution` endpoint.
    - [x] **Frontend**: Add "Revenue Channel" Pie Chart to `AnalyticsBoard.vue`.
    - [x] **Frontend**: Add "Average Order Value" comparison.
    - [x] **Verification**: Validation via Admin Dashboard.

- [x] **Phase 19: Admin Order Search & Filter**
    - [x] **Frontend**: Implement Smart Search Bar in `OrderManagement.vue`.
    - [x] **UX**: Implement "Search Mode" overlay logic.
    - [x] **Verification**: Verify Search/Clear workflow.

# Future Expansion & Productionization

- [ ] **Phase 20: Kitchen Display System (KDS)**
    - [ ] **Frontend**: Create `KitchenOrderView.vue` (Simplified, Large Text).
    - [ ] **UX**: Optimize for tablets (Touch-friendly "Done" actions).
    - [ ] **Real-time**: Ensure WebSocket updates are instant.

- [ ] **Phase 21: Staff Management & RBAC**
    - [ ] **Backend**: Enhance User entity with Roles (ADMIN, STAFF, KITCHEN).
    - [ ] **Frontend**: Create Staff Management UI in Admin Dashboard.
    - [ ] **Security**: Enforce role-based access on API endpoints.

- [ ] **Phase 22: Printer Integration (Simulation)**
    - [ ] **Frontend**: Implement "Print Receipt" feature.
    - [ ] **Output**: Generate PDF receipt with Order details and QR Code.

- [ ] **Phase 23: Customer Facing Display System**
    - [ ] **Frontend**: Create `CustomerDisplay.vue` (Big Screen Mode).
    - [ ] **Features**: Show "Preparing" vs "Ready to Pickup" lists.
    - [ ] **Animation**: Add engaging animations for status updates.

- [ ] **Phase 24: Deployment Prep (Dockerization)**
    - [ ] **DevOps**: Dockerize individual micro-frontends (`host`, `menu`, `admin`).
    - [ ] **DevOps**: Dockerize backend services (`auth`, `order`, `gateway`).
    - [ ] **Orchestration**: Create production-ready `docker-compose.prod.yml`.

# Agentic Workflow Optimization

- [x] **Phase 25: Automated Testing Standardization**
    - [x] **Configuration**: Create `.agent/testing_config.json` with authoritative URLs and Credentials.
    - [x] **Workflow**: Create `.agent/workflows/run_browser_test.md` to enforce config usage.
    - [x] **Workflow**: Create `.agent/workflows/manage_backend.md` to enforce Docker discipline.
    - [x] **Workflow**: Create `.agent/workflows/read_secrets.md` to allow `.env` access.
    - [x] **Documentation**: Update `task.md` (this file) to reflect these standardizations.

- [x] **Phase 26: Frontend Workflow Standardization**
    - [x] **Workflow**: Create `.agent/workflows/manage_frontend.md` to enforce `preview` mode for sub-apps.
