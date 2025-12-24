# Optimization Roadmap

This document tracks high-value feature optimizations identified during the codebase review.

## 1. 🔍 [Frontend] Menu Search & Filter
**Goal**: Enhance user experience by allowing customers to quickly find menu items.
- [x] **Search Bar**: Add a text input to filter menu items by name/description in `MenuList.vue`.
- [x] **Category Filter**: (Optional) Add buttons to filter by category (e.g., "Main", "Drink") - *Requires DB Schema update if 'category' field is missing*.
- [x] **Client-side Filtering**: Implement computed properties in Vue to filter the `menus` array in real-time.

## 2. 📝 [Full Stack] Cart Item Notes
**Goal**: Allow customers to customize their orders (e.g., "No onions", "Less ice").
- [x] **Frontend (`Cart.vue`)**:
    - [x] Add an "Edit Note" button/input for each cart item.
    - [x] Store notes in the Pinia Cart Store.
- [x] **Backend (`OrderService`)**:
    - [x] Update `OrderItem` entity to include a `String note` field.
    - [x] Update `CreateOrderRequest` DTO to accept notes.
- [x] **Admin (`OrderManagement.vue`)**:
    - [x] Display the note in the "Order Items" list.
- [x] **Frontend (`MyOrders.vue`)**:
    - [x] Display note in order history.

## 3. 🍳 [Full Stack] Refined Order Workflow ("Ready" State)
**Goal**: bridge the gap between "Preparing" and "Completed" to manage the pickup process better.
- [x] **Backend (`OrderController`)**:
    - [x] Ensure `PENDING` -> `PREPARING` -> `READY` -> `COMPLETED` transitions are supported.
- [x] **Admin (`OrderManagement.vue`)**:
    - [x] Update "Mark Ready" button logic to set status to `READY`.
    - [x] Add a visual indicator for `READY` orders (different color, e.g., Yellow/Orange).
- [x] **Frontend (`MyOrders.vue`)**:
    - [x] Show a distinct "Ready for Pickup" status card to the user (via Badge styling).

## 4. 🚀 [Admin] Dashboard Optimization (Sorting & Tabs)
**Goal**: Improve order management usability by fixing sort order and adding status filters.
- [x] **Newest First**: Update backend to sort `getAllOrders` by `createdAt DESC`.
- [x] **Status Tabs**: Refactor Admin Dashboard to tabbed view (Active, Pending, Kitchen, Counter, History).
- [x] **Real-time**: Ensure WebSocket updates propagate correctly to the filtered views.

## 5. 🛑 [Full Stack] Customer Order Cancellation
**Goal**: Allow customers to cancel their own `PENDING` orders to reduce friction and food waste.
- [x] **Backend**: Implement `cancelOrder` (PATCH) with strict state validation (PENDING only).
- [x] **Frontend (`MyOrders.vue`)**: Add "Cancel" button with custom confirmation dialog.
- [x] **Security**: Ensure users can only cancel their own orders.
