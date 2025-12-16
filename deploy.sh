#!/usr/bin/env bash
set -euo pipefail

# Kiểm tra biến môi trường
VPS_USER="${VPS_USER:?VPS_USER is required}"
VPS_HOST="${VPS_HOST:?VPS_HOST is required}"

DOCKER_IMAGE="maingoctu56/management-system"
SERVICE_NAME="management-system"
NETWORK_NAME="app-network"
REPLICAS=2
ENV_FILE="/etc/app/management-system.env"

echo "🚀 Starting deployment to $VPS_HOST..."
echo "📦 Image: $DOCKER_IMAGE:latest"
echo "🔧 Service: $SERVICE_NAME"

# SSH vào VPS và chạy các lệnh
ssh -o StrictHostKeyChecking=no "$VPS_USER@$VPS_HOST" bash <<'EOF'
set -euo pipefail

DOCKER_IMAGE="$DOCKER_IMAGE"
SERVICE_NAME="$SERVICE_NAME"
NETWORK_NAME="$NETWORK_NAME"
REPLICAS=$REPLICAS
ENV_FILE="$ENV_FILE"

echo "📥 Pulling latest Docker image..."
docker pull $DOCKER_IMAGE:latest

echo "🌐 Checking/Creating network..."
if ! docker network inspect $NETWORK_NAME >/dev/null 2>&1; then
    echo "Creating new overlay network..."
    docker network create --driver overlay --attachable $NETWORK_NAME
else
    echo "Network $NETWORK_NAME already exists"
fi

# Đọc biến từ file env trên VPS
if [ -f "$ENV_FILE" ]; then
    echo "📋 Reading environment variables from $ENV_FILE"
    source $ENV_FILE

    DB_CONNECTION_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
    echo "🔗 Database URL: $DB_CONNECTION_URL"
else
    echo "❌ ENV file not found at $ENV_FILE"
    exit 1
fi

echo "🔍 Checking if service exists..."
if docker service ls --filter name=$SERVICE_NAME --format '{{.Name}}' | grep -q "^$SERVICE_NAME\$"; then
    echo "🔄 Updating existing service..."

    docker service update \
        --image $DOCKER_IMAGE:latest \
        --env-add SPRING_DATASOURCE_URL="$DB_CONNECTION_URL" \
        --env-add SPRING_DATASOURCE_USERNAME="$DB_USER" \
        --env-add SPRING_DATASOURCE_PASSWORD="$DB_PASS" \
        $SERVICE_NAME

    # Thêm network nếu chưa có
    docker service update --network-add $NETWORK_NAME $SERVICE_NAME 2>/dev/null || echo "✓ Network already attached"
else
    echo "🆕 Creating new service..."

    docker service create \
        --name $SERVICE_NAME \
        --replicas $REPLICAS \
        --network $NETWORK_NAME \
        --publish 8080:8080 \
        --env SPRING_DATASOURCE_URL="$DB_CONNECTION_URL" \
        --env SPRING_DATASOURCE_USERNAME="$DB_USER" \
        --env SPRING_DATASOURCE_PASSWORD="$DB_PASS" \
        --update-parallelism 1 \
        --update-delay 10s \
        --restart-condition on-failure \
        --restart-max-attempts 3 \
        $DOCKER_IMAGE:latest
fi

echo "⏳ Waiting for service to stabilize..."
sleep 5

echo "🧹 Cleaning up old images..."
docker image prune -af --filter "until=24h" || true

echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "📊 Service status:"
docker service ls --filter name=$SERVICE_NAME
echo ""
echo "📋 Service tasks:"
docker service ps $SERVICE_NAME --no-trunc | head -n 5
echo ""
echo "📝 Recent logs (last 20 lines):"
docker service logs $SERVICE_NAME --tail 20 2>&1 | grep -v "^$" || echo "No logs available yet"

EOF

echo ""
echo "✅ Deployment script finished!"
echo "🌐 Application should be available at: http://$VPS_HOST:8080"