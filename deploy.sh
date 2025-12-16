#!/usr/bin/env bash
set -euo pipefail

VPS_USER="${VPS_USER:?required}"
VPS_HOST="${VPS_HOST:?required}"

DOCKER_IMAGE="maingoctu56/management-system"
SERVICE_NAME="management-system"
NETWORK_NAME="app-network"
REPLICAS=2
ENV_FILE="/etc/app/management-system.env"

ssh "$VPS_USER@$VPS_HOST" bash <<EOF
set -euo pipefail

docker pull $DOCKER_IMAGE:latest

docker network inspect $NETWORK_NAME >/dev/null 2>&1 || \
docker network create --driver overlay $NETWORK_NAME

if docker service ls --format '{{.Name}}' | grep -q "^$SERVICE_NAME\$"; then
  docker service update \
    --image $DOCKER_IMAGE:latest \
    --env-file $ENV_FILE \
    --update-parallelism 1 \
    --update-delay 10s \
    --update-failure-action rollback \
    $SERVICE_NAME
else
  docker service create \
    --name $SERVICE_NAME \
    --network $NETWORK_NAME \
    -p 8080:8080 \
    --replicas $REPLICAS \
    --env-file $ENV_FILE \
    $DOCKER_IMAGE:latest
fi
EOF
