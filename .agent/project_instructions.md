
# Project Instructions

Every time you start a new conversation in this workspace, you MUST first read the following files to anchor yourself in the project's context, architecture, and design rules:

1.  `README.md`: System overview, core features, and quick start guide.
2.  `SYSTEM_DESIGN.md`: Detailed architecture, API specs, and technical decisions.

These files are the **Source of Truth** for this project. All code changes and feature implementations must adhere to the patterns and guidelines defined therein.

## 3. Operational Standards (MANDATORY)
This project has strict operational workflows defined in `.agent/workflows/`. You **MUST** follow them:
- **Testing**: Use `/run_browser_test` (reads `.agent/testing_config.json`).
- **Backend**: Use `/manage_backend` (Docker Compose ONLY).
- **Frontend**: Use `/manage_frontend` (Host=dev, Remote=preview).
- **Secrets**: Use `/read_secrets` (Allowed to read .env).

**Do NOT guess** URLs, credentials, or startup commands. Use the provided workflows.
