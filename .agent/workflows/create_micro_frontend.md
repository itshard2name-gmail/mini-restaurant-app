---
description: Guide for creating new Micro-Frontend Sub-Apps with correct CSS isolation
---
# Creating a New Micro-Frontend Sub-App

When adding a new sub-application (e.g., via Vite + Module Federation), you MUST follow these CSS isolation rules to prevent conflicts with the Host App.

## 1. CSS Isolation Strategy: ID Scoping (Best Practice)
To prevent Sub-App styles (Tailwind utilities) from leaking into the Host App, we use **ID Scoping**.

### Step 1: Configure Tailwind
In `tailwind.config.js`, add the `important` property with a unique ID selector:

```javascript
/* tailwind.config.js */
export default {
    // ...
    important: '#sub-app-unique-id', // All utilities will be generated as #sub-app-unique-id .utility
    // ...
}
```

### Step 2: Wrap Your Exposed Component
In your main exposed component (e.g., `AdminDashboard.vue`), wrap the entire template in a div with that ID:

```vue
<template>
  <div id="sub-app-unique-id">
     <!-- Your content here -->
  </div>
</template>
```

### Step 3: Use Standard Imports
You can now safely import your styles. Even global resets (if any) will likely be scoped or less impactful, but sticking to utilities is still safest.

```javascript
/* You can likely stick to standard styles now, or use the utilities file if you want to be extra safe */
// import './style.css'; 
```

## 2. Checklist for New Sub-Apps
- [ ] Unique ID chosen for Sub-app (e.g., `#sub-app-admin`)
- [ ] `tailwind.config.js` updated with `important: '#id'`
- [ ] Root component wrapped with `<div id="id">`
- [ ] Verify: Navbar layout in Host App should remain unaffected.
