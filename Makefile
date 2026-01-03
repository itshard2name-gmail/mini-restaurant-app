# Makefile for managing Dev and Stage environments

.PHONY: dev stage down-dev down-stage

# Development Environment (Backend in Docker, Frontend Local)
# Uses standard PORT_... variables from .envrc
dev:
	@echo "Starting Development Environment (Full Docker)..."
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build

# Build all Frontends (Required for Dev Docker Mounts)
build-frontends:
	cd frontend/host-app && npm install && npm run build
	cd frontend/sub-app-menu && npm install && npm run build
	cd frontend/sub-app-admin && npm install && npm run build

# Staging Environment (Full Docker)
# Uses STAGE_PORT_... variables from .envrc
stage:
	@echo "Starting Staging Environment..."
	docker compose -f docker-compose.yml -f docker-compose.stage.yml up -d --build

# Stop Dev
down-dev:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml down

# Stop Stage
down-stage:
	docker compose -f docker-compose.yml -f docker-compose.stage.yml down

# Get Quick Tunnel URL (Wait a few seconds after starge)
tunnel-url:
	@docker logs stage-tunnel 2>&1 | grep -o 'https://.*\.trycloudflare.com' | head -n 1

# Clean Order Database (Truncate tables ignoring FKs)
clean-db:
	@echo "Truncating orders and order_items..."
	docker compose exec -T mysql mysql -u root -ppassword order_db -e "SET FOREIGN_KEY_CHECKS = 0; TRUNCATE TABLE order_item; TRUNCATE TABLE orders; SET FOREIGN_KEY_CHECKS = 1;"
	@echo "Database cleaned."
