# Payment System Fixes & Handoff Summary
**Date**: 2026-01-03
**Topic**: Payment Verification & Critical Bug Fixes (500 Errors, Authorization)

## 1. Executive Summary
This session focused on debugging and stabilizing the **Hybrid Payment System** (Online vs. Offline/Cash). We encountered and resolved several high-priority `500 Internal Server Error` issues preventing users and guests from completing orders.

**Current Status**: 
- ✅ **Online Payment (Credit Card)**: Fully functional. Redirects to Mock Gateway and updates status to `PAID`.
- ✅ **Offline Payment (Cash)**: Fully functional. Verified for both Registered Users and Guest (QR Scan) users.
- 🚧 **Admin Verification**: Pending validation of the "Cash Register" UI in the Admin Dashboard.

---

## 2. Issues Resolved

### A. Mock Gateway 500 Error (Crash)
- **Symptom**: Accessing the Mock Gateway page crashed with a 500 error.
- **Root Cause**: `java.util.UnknownFormatConversionException` in `MockPaymentController.java`. The CSS contained `width: 100%;`. `String.formatted()` interpreted `%` as a format specifier.
- **Fix**: Escaped percent signs to `width: 100%%;`.

### B. "Pay Now" Button Persistence
- **Symptom**: After successful payment, the "Pay Now" button remained visible in `MyOrders.vue`.
- **Root Cause**: Backend `completePayment` updated `PaymentStatus` to `PAID` but left the main `Order.status` as `PENDING`/`COMPLETED`. Frontend relies on `Order.status`.
- **Fix**: Updated `OrderService.completePayment` to explicitly set `order.setStatus("PAID")`.

### C. Cash Payment 500 Error (User Ownership)
- **Symptom**: Clicking "Pay Now" for a **Cash** order failed with 500 "Unauthorized" for registered users.
- **Root Cause**: The frontend function `executePayOrder` in `MyOrders.vue` was missing the `X-User-Id` header. The backend rejected the request because requests for user-owned orders must carry this header.
- **Fix**: Updated `MyOrders.vue` to append `X-User-Id` to the request config.

### D. Guest Payment 500 Error (Hybrid Access)
- **Symptom**: Guest users (scanning QR code) failed to pay for orders they just created if the system had "merged" them or tracked them via shadow tokens, resulting in a complex "Unauthorized" error.
- **Root Cause**: `OrderService.getOrder` (and related checks) enforced strict User ID matching. It did not allow access if the request had a valid `guestToken` but no `X-User-Id`, even if the order contained that `guestToken`.
- **Fix**: Implemented **Hybrid Authorization** in `OrderService.java`. Access is granted if:
    1. `X-User-Id` matches `Order.userId` **OR**
    2. `guestToken` (param) matches `Order.guestToken`.

---

## 3. Key Files & Locations

| Component | File Path | Usage |
| :--- | :--- | :--- |
| **Backend Service** | `backend-services/order-service/src/main/java/com/example/order/service/OrderService.java` | Core logic for Auth, Payment, and Status Updates. |
| **Controller** | `backend-services/order-service/src/main/java/com/example/order/controller/OrderController.java` | Endpoints: `POST /pay`, `POST /callback`, `PATCH /payment-method`. |
| **Frontend (Menu)** | `frontend/sub-app-menu/src/components/MyOrders.vue` | Customer "Pay Now" Modal & Logic. **Crucial Fix applied here**. |
| **Frontend (Cart)** | `frontend/sub-app-menu/src/components/Cart.vue` | Submit Order logic (Modified to hide payment for Dine-In). |
| **E2E Tests** | `tests/e2e/payment.spec.js` | Main payment flow verification tests. |
| **E2E Tests** | `tests/e2e/cash_payment.spec.js` | Targeted reproduction test for Cash failures (Useful for future regression). |

---

## 4. Pending Tasks / Next Steps (Handoff)

The immediate next step is to **Verify Admin Capabilities**.

### Task 1: Admin Cash Register Verification
We need to confirm that the Store Owner can accept the cash payment.
- **Action**: Log into Admin Dashboard (`http://localhost:3000` or `http://localhost:10000/admin`).
- **Scenario**:
    1. Create a "Dine-In" order with "Cash" payment (User side).
    2. Go to Admin Dashboard -> Active Orders.
    3. Locate the order (Status: PENDING, Payment: UNPAID).
    4. Click **"💰 Pay (Cash)"** button.
    5. Enter "Amount Tendered" (e.g., $100 for a $20 order).
    6. Verify "Change Due" calculation ($80).
    7. Click "Confirm".
    8. **Verify**: Order Status changes to `PAID` (Green Badge).

### Task 2: Robustness of Playwright Tests
- The E2E test `payment.spec.js` occasionally times out on the Redirect verification steps due to race conditions in the test runner.
- **Action**: Add explicit waits or retry logic to the test to make it greener in CI/CD.

### Task 3: Cleanup
- Remove temporary debug logs (`system.out.println`) from `OrderService.java`.
- Remove the temporary `tests/e2e/cash_payment.spec.js` file if no longer needed, or merge it into the main suite.

---

## 5. How to Continue
1. **Start Environment**: Ensure `docker-compose up` is running.
2. **Check Logs**: Monitor `docker logs -f dev-order-service` during Admin interactions.
3. **Run Admin Test**: You may want to create a new `tests/e2e/admin_payment.spec.js` following the scenario in Task 1.
