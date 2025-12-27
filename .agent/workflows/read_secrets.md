---
description: Guide for Accessing Secrets in .env file
---

# Read Secrets Workflow

This workflow clarifies the permissions and method for accessing local development secrets.

## 1. The GitIgnore Misconception
- **Fact**: The `.env` file is listed in `.gitignore` to prevent it from being pushed to GitHub.
- **Permission**: This **DOES NOT** mean the Agent is forbidden from reading it. You **ARE AUTHORIZED** to read `.env` to get necessary tokens, keys, or passwords for local testing.

## 2. How to Read
- **Action**: Simply read the file content.
- **Command**: `view_file .env` or `cat .env`

## 3. Usage
- Use the values found in `.env` (like `JWT_SECRET`, `DB_PASSWORD`) to configure your test tools or verify environment consistency.
- **Security Check**: Do **NOT** output the full content of `.env` into a public artifact or chat message unless necessary. Use the values internally for tool execution.
