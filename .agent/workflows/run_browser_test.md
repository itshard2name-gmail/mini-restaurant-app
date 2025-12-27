---
description: Guide for running browser-based automated tests or verifications
---

# Run Browser Test Workflow

This workflow MUST be followed by any Agent performing browser automation to ensure consistency and avoid "guessing" URLs or Credentials.

## 1. Load Configuration
First, read the authoritative configuration file to get the correct Environment URLs and Credentials.
- **Action**: Read the file `.agent/testing_config.json`
- **Command**: `read_file .agent/testing_config.json`

## 2. Determine Target Context
Based on the task at hand, decide which "App" you need to test:
- **Admin Dashboard**: Use `base_urls.admin_panel` (e.g., `http://localhost:3000/admin`)
- **Customer Storefront**: Use `base_urls.customer_app` (e.g., `http://localhost:3000`)

## 3. Prepare Browser Session
- **Action**: Launch the browser subagent.
- **Initial URL**: Pass the **exact** URL found in config Step 2. Do NOT invent URLs like `/login` or `/dashboard` unless strictly necessary after the base URL.

## 4. Authentication (If Login Required)
If the test requires a logged-in state:
1.  **Retrieve Credentials**: specific to the role needed (Admin vs Customer) from the config file.
2.  **Input**: Use the `username` and `password` from the config.
    - *Note*: Do NOT guess credentials like `admin/admin`. ALWAYS use the config values.

## 5. Execution
Proceed with the specific test steps (clicking, navigating) as required by the user's request.

## 6. Failure Handling
If the URL does not load:
- Check if the Frontend server is running.
- **Do NOT** try to "fix" the URL by guessing `8080` or other ports unless you have verified the frontend is down and are intentionally testing the backend gateway directly.
