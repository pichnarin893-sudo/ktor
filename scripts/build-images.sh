#!/bin/bash

# Build Docker Images Script
# Usage: ./scripts/build-images.sh [service-name] [version]
# Example: ./scripts/build-images.sh auth-service v1.0.0
# Example: ./scripts/build-images.sh all latest

set -e

# Configuration
REGISTRY="${DOCKER_REGISTRY:-ghcr.io/yourusername}"  # Change 'yourusername' to your GitHub username
VERSION="${2:-latest}"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Services to build
SERVICES=("auth-service" "inventory-service" "order-service" "telegram-bot-service")

# Function to build a single service
build_service() {
    local service=$1
    local version=$2

    echo -e "${BLUE}Building ${service}:${version}...${NC}"

    docker build \
        --file "${PROJECT_ROOT}/${service}/Dockerfile" \
        --tag "${REGISTRY}/${service}:${version}" \
        --tag "${REGISTRY}/${service}:latest" \
        --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
        --build-arg VERSION="${version}" \
        "${PROJECT_ROOT}"

    echo -e "${GREEN}✓ Successfully built ${service}:${version}${NC}\n"
}

# Main logic
SERVICE_NAME="${1:-all}"

if [ "$SERVICE_NAME" = "all" ]; then
    echo -e "${BLUE}Building all services...${NC}\n"
    for service in "${SERVICES[@]}"; do
        build_service "$service" "$VERSION"
    done
    echo -e "${GREEN}✓ All services built successfully!${NC}"
else
    # Check if service exists
    if [[ ! " ${SERVICES[@]} " =~ " ${SERVICE_NAME} " ]]; then
        echo -e "${YELLOW}Error: Service '${SERVICE_NAME}' not found.${NC}"
        echo -e "Available services: ${SERVICES[*]}"
        exit 1
    fi

    build_service "$SERVICE_NAME" "$VERSION"
fi

# List built images
echo -e "\n${BLUE}Built images:${NC}"
docker images | grep "${REGISTRY}" | head -n 8
