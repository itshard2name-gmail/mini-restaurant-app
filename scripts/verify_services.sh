#!/bin/bash

echo "Checking Service Health..."

# 1. Registry Server (Eureka)
echo "1. Registry Server (localhost:10020)..."
if curl -s http://localhost:10020/actuator/health | grep "UP"; then
    echo "   [OK] Registry Server is UP"
else
    echo "   [FAIL] Registry Server is responding or DOWN"
fi

# 2. Gateway Service
echo "2. Gateway Service (localhost:10010)..."
if curl -s http://localhost:10010/actuator/health | grep "UP"; then
    echo "   [OK] Gateway Service is UP"
else
    echo "   [FAIL] Gateway Service is DOWN"
fi

# 3. Auth Service (Internal, verify via Gateway if possible or direct if exposed)
# dev-auth-service does NOT expose port to host in docker-compose.dev.yml explicitly?
# Wait, docker-compose.yml defines it but no ports. docker-compose.dev.yml defines no ports for auth-service either.
# It is only accessible via Gateway/Envoy.
# We will check via Gateway Info or just assume loopback if they shared network, but from Host we must check via Gateway.
echo "3. Auth Service (via Gateway)..."
# Assuming /api/auth/health or just check if Gateway routes to it. 
# We'll rely on docker ps for existence first.
if docker ps | grep dev-auth-service; then
     echo "   [OK] Auth Service Container is Running"
else
     echo "   [FAIL] Auth Service Container is NOT Running"
fi

# 4. Order Service (via Gateway)
echo "4. Order Service (via Gateway)..."
if docker ps | grep dev-order-service; then
     echo "   [OK] Order Service Container is Running"
else
     echo "   [FAIL] Order Service Container is NOT Running"
fi

# 5. Frontend Host
echo "5. Frontend Host (localhost:10000)..."
if curl -I -s http://localhost:10000 | grep "200 OK"; then
    echo "   [OK] Frontend Host is Serving"
else
    echo "   [WARN] Frontend Host might be down or returning non-200"
fi
