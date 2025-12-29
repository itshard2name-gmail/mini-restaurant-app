# Infrastructure & Environment Variables Map

This document defines the infrastructure port mapping and environment variable standards for the **swift-mariner** project.
**Agents and Developers** should consult this file when modifying `docker-compose.yml` or setting up local environments.

## Project Identity
- **Project Name**: apcs-practice
- **Base Port**: 3010

## Environment Variable Schema

The following variables are defined in `.envrc` and should be used in all configuration files.

### Development Environment (Local Terminal)
These ports are for running services directly in the terminal (Hot Reload compatible).

| Variable Name | Value | Component | Description |
| :--- | :--- | :--- | :--- |
| `APCS_PRATICE_FRONTEND_PORT` | `3010` | Frontend | Vue 3 / Vite dev server |
| `APCS_PRATICE_BACKEND_PORT` | `3011` | Backend | API Server (Node) |
| `APCS_PRATICE_DB_PORT` | `5433` | Database | Docker container mapped port for local dev access |

### Staging Environment (Docker Compose)
These ports are for running the full stack in Docker (Integration Test compatible).

| Variable Name | Value | Component | Description |
| :--- | :--- | :--- | :--- |
| `APCS_PRATICE_STG_FRONTEND_PORT` | `3050` | Frontend | Full build container |
| `APCS_PRATICE_STG_BACKEND_PORT  ` | `3051` | Backend | API Server container |
| `APCS_PRATICE_STG_DB_PORT` | `5434` | Database | Staging DB container |

## Implementation Guide for Agents

When generating `docker-compose.yml` or standard `.env` files, **ALWAYS** use these explicit variable names.

### Example: docker-compose.staging.yml

```yaml
services:
  frontend:
    ports:
      - "${APCS_PRATICE_STG_FRONTEND_PORT}:3000"
    environment:
      - API_URL=http://backend:${APCS_PRATICE_STG_BACKEND_PORT} # Or internal port if using network

  backend:
    ports:
      - "${ APCS_PRATICE_STG_BACKEND_PORT}:3011"
    environment:
      - DB_PORT=5432 # Internal docker network port
```
