---
description: Guide for updating the User Manual (v2.0) and regenerating screenshots.
---

This workflow guides you through the process of updating `docs/USER_MANUAL.md` and refreshing the automated screenshots.

## Prerequisites
Ensure the application is running locally.
1.  **Backend**: `docker-compose up -d` (All services must be UP).
2.  **Frontend**:
    *   Host App: `npm run dev` in `frontend/host-app`.
    *   Sub Apps: `npm run preview` in `frontend/sub-app-menu` and `frontend/sub-app-admin`.

## 1. Refresh Screenshots (Automated)
Run the Playwright automation script to generate fresh screenshots based on the current UI.

// turbo
1. Navigate to the Host App directory
   `cd frontend/host-app`

2. Run the screenshot generation script
   `npm run docs:screenshots`

3. Verify output
   Check `docs/manual-screenshots/` to ensure images like `customer_menu.png`, `dine_in_menu.png`, and `admin_dashboard_active.png` have been updated.

## 2. Update Documentation
If new features were added or user flows changed, update the text to match.

1.  **Read** `docs/USER_MANUAL.md`.
2.  **Compare** the text with the new screenshots/features.
3.  **Edit** the markdown file to reflect changes (e.g., new buttons, new login steps).
4.  **Preview** the markdown to ensure image links are correct.

## 3. Troubleshooting
*   **Script Failures**: If `npm run docs:screenshots` fails, check if the UI selectors in `frontend/host-app/scripts/generate-screenshots.cjs` match the current codebase.
*   **Timeouts**: Increase the `timeout` values in the `.cjs` script if the local server is slow.
