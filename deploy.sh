#!/usr/bin/env bash
set -euo pipefail

# Kiểm tra biến môi trường bắt buộc
: "${VPS_USER:?VPS_USER is required}"
: "${VPS_HOST:?VPS_HOST is required}"
: "${DOCKER_IMAGE:=maingoctu56/management-system}"
: "${SERVICE_NAME:=management-system}"
: "${NETWORK_NAME:=app-network}"
: "${REPLICAS:=2}"
: "${ENV_FILE:=/etc/app/management-system.env}"

echo "🚀 Starting deployment to $VPS_HOST..."
echo "📦 Image: $DOCKER_IMAGE:latest"
echo "🔧 Service: $SERVICE_NAME"
echo "🌐 Network: $NETWORK_NAME"
echo "📊 Replicas: $REPLICAS"
echo ""

# SSH vào VPS và thực thi deployment
# Truyền biến môi trường vào SSH session
ssh -o StrictHostKeyChecking=no "$VPS_USER@$VPS_HOST" \
  DOCKER_IMAGE="$DOCKER_IMAGE" \
  SERVICE_NAME="$SERVICE_NAME" \
  NETWORK_NAME="$NETWORK_NAME" \
  REPLICAS="$REPLICAS" \
  ENV_FILE="$ENV_FILE" \
  bash <<'ENDSSH'
set -euo pipefail

echo "📥 Pulling latest Docker image..."
docker pull $DOCKER_IMAGE:latest

echo ""
echo "🌐 Checking/Creating network..."
if ! docker network inspect $NETWORK_NAME >/dev/null 2>&1; then
    echo "Creating new overlay network with attachable flag..."
    docker network create --driver overlay --attachable $NETWORK_NAME
else
    echo "✓ Network $NETWORK_NAME already exists"
fi

# Đọc biến môi trường từ file trên VPS
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ ERROR: ENV file not found at $ENV_FILE"
    echo "Please create the file with the following content:"
    echo "DB_HOST=mysql-db"
    echo "DB_PORT=3306"
    echo "DB_NAME=MyApp"
    echo "DB_USER=root"
    echo "DB_PASS=your_password"
    exit 1
fi

echo ""
echo "📋 Reading environment variables from $ENV_FILE"
source "$ENV_FILE"

# Validate required variables
if [ -z "${DB_HOST:-}" ] || [ -z "${DB_PORT:-}" ] || [ -z "${DB_NAME:-}" ]; then
    echo "❌ ERROR: Missing required database variables in $ENV_FILE"
    echo "Required variables: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS"
    exit 1
fi

DB_CONNECTION_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "✓ Database connection: $DB_CONNECTION_URL"
echo "✓ Database user: $DB_USER"

echo ""
echo "🔍 Checking if service exists..."
if docker service ls --filter name=$SERVICE_NAME --format '{{.Name}}' | grep -q "^$SERVICE_NAME$"; then
    echo "🔄 Updating existing service..."

    docker service update \
        --image $DOCKER_IMAGE:latest \
        --env-add SPRING_DATASOURCE_URL="$DB_CONNECTION_URL" \
        --env-add SPRING_DATASOURCE_USERNAME="$DB_USER" \
        --env-add SPRING_DATASOURCE_PASSWORD="$DB_PASS" \
        $SERVICE_NAME

    echo ""
    echo "🔗 Ensuring service is connected to $NETWORK_NAME..."
    docker service update --network-add $NETWORK_NAME $SERVICE_NAME 2>/dev/null || \
        echo "✓ Network already attached"
else
    echo "🆕 Creating new service..."

    docker service create \
        --name $SERVICE_NAME \
        --replicas $REPLICAS \
        --network $NETWORK_NAME \
        --publish published=8080,target=8080 \
        --env SPRING_DATASOURCE_URL="$DB_CONNECTION_URL" \
        --env SPRING_DATASOURCE_USERNAME="$DB_USER" \
        --env SPRING_DATASOURCE_PASSWORD="$DB_PASS" \
        --update-parallelism 1 \
        --update-delay 10s \
        --update-failure-action rollback \
        --restart-condition on-failure \
        --restart-max-attempts 3 \
        --restart-delay 5s \
        $DOCKER_IMAGE:latest

    echo "✓ Service created successfully"
fi

echo ""
echo "⏳ Waiting for service to stabilize..."
sleep 10

echo ""
echo "📊 Current service status:"
docker service ps $SERVICE_NAME --format "table {{.Name}}\t{{.CurrentState}}\t{{.Error}}" 2>&1 | head -n 5

echo ""
echo "🧹 Cleaning up old images..."
docker image prune -af --filter "until=24h" 2>&1 | grep -v "^$" || true

echo ""
echo "✅ Deployment completed successfully!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

ENDSSH

echo ""
echo "✅ Deployment script finished!"
echo "🌐 Application should be available at: http://$VPS_HOST:8080"
echo ""