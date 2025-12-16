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
ssh -o StrictHostKeyChecking=no "$VPS_USER@$VPS_HOST" bash <<EOF
set -euo pipefail

DOCKER_IMAGE="$DOCKER_IMAGE"
SERVICE_NAME="$SERVICE_NAME"
NETWORK_NAME="$NETWORK_NAME"
REPLICAS=$REPLICAS
ENV_FILE="$ENV_FILE"

echo "📥 Pulling latest Docker image..."
docker pull \$DOCKER_IMAGE:latest

echo "🌐 Checking/Creating network..."
docker network inspect \$NETWORK_NAME >/dev/null 2>&1 || \
docker network create --driver overlay \$NETWORK_NAME

echo "🔍 Checking if service exists..."
if docker service ls --filter name=\$SERVICE_NAME --format '{{.Name}}' | grep -q "^\$SERVICE_NAME\$"; then
    echo "🔄 Updating existing service..."
    docker service update --image \$DOCKER_IMAGE:latest \$SERVICE_NAME
else
    echo "🆕 Creating new service..."

    # Kiểm tra file env có tồn tại không
    if [ -f "\$ENV_FILE" ]; then
        docker service create \\
            --name \$SERVICE_NAME \\
            --replicas \$REPLICAS \\
            --network \$NETWORK_NAME \\
            --publish 8080:8080 \\
            --env-file \$ENV_FILE \\
            --update-parallelism 1 \\
            --update-delay 10s \\
            \$DOCKER_IMAGE:latest
    else
        echo "⚠️  ENV file not found at \$ENV_FILE, creating service without env file..."
        docker service create \\
            --name \$SERVICE_NAME \\
            --replicas \$REPLICAS \\
            --network \$NETWORK_NAME \\
            --publish 8080:8080 \\
            --update-parallelism 1 \\
            --update-delay 10s \\
            \$DOCKER_IMAGE:latest
    fi
fi

echo "🧹 Cleaning up old images..."
docker image prune -af --filter "until=24h" || true

echo "✅ Deployment completed successfully!"
echo ""
echo "📊 Service status:"
docker service ls --filter name=\$SERVICE_NAME
echo ""
docker service ps \$SERVICE_NAME --no-trunc
EOF

echo ""
echo "✅ Deployment script finished!"