---
description: Start the development or staging environment
---

To start the **Development Environment** (Recommended for coding):

1. Allow .envrc (One time)
   ```bash
   direnv allow
   ```

2. Build Frontends
   ```bash
   make build-frontends
   ```

3. Start Environment (Full Docker)
   // turbo
   ```bash
   make dev
   ```

   *Host App*: http://localhost:10000


To start the **Staging Environment** (Full Docker):

1. Allow .envrc (One time)
   ```bash
   direnv allow
   ```

2. Start Everything
   // turbo
   ```bash
   make stage
   ```
