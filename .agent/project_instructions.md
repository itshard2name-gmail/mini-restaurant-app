
# Project Instructions

Every time you start a new conversation in this workspace, you MUST first read the following files to anchor yourself in the project's context, architecture, and design rules:

1.  `README.md`: System overview, core features, and quick start guide.
2.  `SYSTEM_DESIGN.md`: Detailed architecture, API specs, and technical decisions.

These files are the **Source of Truth** for this project. All code changes and feature implementations must adhere to the patterns and guidelines defined therein.

## 3. Operational Standards (MANDATORY)
This project has strict operational workflows defined in `.agent/workflows/`. You **MUST** follow them:
- **Testing**: Use `/run_browser_test` (reads `.agent/testing_config.json`).
- **Backend**: Use `/manage_backend` (Docker Compose ONLY). All services run in containers.
- **Frontend**: Use `/manage_frontend`. **ALL** frontend apps (Host & Sub-Apps) run in Docker containers. 
    - **Development Rule**: When modifying frontend code (e.g., Vue components), you MUST publish the changes to the corresponding Docker container (e.g., `npm run build` locally then restart container, or rebuild image) to see the effects. Direct local serving (outside Docker) is NOT supported for full system integration.
- **Secrets**: Use `/read_secrets` (Allowed to read .env).

**Do NOT guess** URLs, credentials, or startup commands. Use the provided workflows.
