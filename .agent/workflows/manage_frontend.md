---
description: Guide for Running Frontend Micro-Frontends (Host vs Sub-apps)
---

# Manage Frontend Workflow

This workflow enforces the strict startup requirements for the Micro-Frontend (Module Federation) architecture.

## 1. The Golden Rule of Federation
To ensure `remoteEntry.js` is correctly generated and exposed to the Host App, you **MUST** use different modes for Host and Remotes.

| App Type | Role | Required Command | Reason |
| :--- | :--- | :--- | :--- |
| **Host App** | Shell / Container | `npm run dev` | Fastest Hot, supports loading external remotes. |
| **Sub-Apps** | Remote (Menu, Admin) | `npm run preview` | **CRITICAL**: `dev` mode often fails to expose `remoteEntry.js` correctly in Vite Federation. `preview` serves the build output reliably. |

## 2. Startup Sequence
Always start the Sub-apps (Remotes) *before* or *simultaneously* with the Host app.

### Step A: Start Remotes (Sub-Apps)
1.  Navigate to `frontend/sub-app-menu`
    - Run: `npm run build` (Ensure fresh build)
    - Run: `npm run preview`
2.  Navigate to `frontend/sub-app-admin`
    - Run: `npm run build` (Ensure fresh build)
    - Run: `npm run preview`

### Step B: Start Host (Shell)
1.  Navigate to `frontend/host-app`
    - Run: `npm run dev`

## 3. Verification
Before declaring success, verify that the Host App can load the remotes.
- **Action**: Check Browser Console on `http://localhost:3000`.
- **Success**: No "Failed to load script" errors for `remoteEntry.js`.
- **Failure**: If you see 404 for `remoteEntry.js`, you likely ran a Sub-app in `dev` mode instead of `preview`. **STOP** and restart that sub-app in `preview`.
