---
description: Guide for Managing Backend Services (Docker Compose vs Java)
---

# Manage Backend Workflow

This workflow enforces strict discipline for managing backend services to prevent "Ghost Services" and configuration drift.

## 1. Golden Rule: Docker Compose Only
- **Constraint**: You **MUST** use `docker compose` to start, stop, or restart backend services.
- **Prohibition**: **NEVER** use `java -jar` to run backend microservices (`auth-service`, `order-service`, etc.) directly on the host machine, unless explicitly instructed by the user for debugging a specific single-instance issue.
    - *Why?* Running `java` directly bypasses the internal network (`servicename:port`) defined in Docker, causing connection failures and port conflicts.

## 2. Pre-Flight Check (Kill Ghost Processes)
Before running `docker compose up`, always ensure no loose Java processes are hugging the ports.

- **Action**: Check for processes on port 8080 or 8761.
- **Command**: `lsof -i :8080` or `ps aux | grep java`
- **Cleanup**: If you see a `java` process that looks like a backend service (and you are trying to start Docker), **KILL IT**.
    - `kill -9 <PID>`

## 3. Starting Services
- **Command**: `docker compose up -d` (Detached mode).
- **Wait**: Allow 10-20 seconds for services (especially MySQL and Eureka) to initialize before checking health.

## 4. Configuration Integrity
- **Prohibition**: **NEVER** modify `docker-compose.yml` to "fix" a startup error (e.g., commenting out a service, changing a port) without **EXPLICIT** user approval.
- **Error Handling**: If `docker compose` fails, analyze the logs (`docker compose logs <service>`). Do not blindly change the config.

## 5. Restarting a Single Service
If you need to restart just one service (e.g., `order-service`):
- **Command**: `docker compose restart order-service`
