#!/bin/bash

# Push Docker Images to Registry Script
# Usage: ./scripts/push-images.sh [service-name] [version]
# Example: ./scripts/push-images.sh auth-service v1.0.0
# Example: ./scripts/push-images.sh all latest

set -e

# Configuration
REGISTRY="${DOCKER_REGISTRY:-ghcr.io/yourusername}"  # Change 'yourusername' to your GitHub username
VERSION="${2:-latest}"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Services to push
SERVICES=("auth-service" "inventory-service" "order-service" "telegram-bot-service")

# Function to push a single service
push_service() {
    local service=$1
    local version=$2

    echo -e "${BLUE}Pushing ${service}:${version}...${NC}"

    # Push versioned tag
    docker push "${REGISTRY}/${service}:${version}"

    # Push latest tag
    docker push "${REGISTRY}/${service}:latest"

    echo -e "${GREEN}✓ Successfully pushed ${service}:${version}${NC}\n"
}

# Check if user is logged in to registry
echo -e "${BLUE}Checking registry authentication...${NC}"
if ! docker info 2>&1 | grep -q "Username"; then
    echo -e "${YELLOW}Warning: You may not be logged in to the Docker registry.${NC}"
    echo -e "${YELLOW}Run: docker login ${REGISTRY%%/*}${NC}\n"
fi

# Main logic
SERVICE_NAME="${1:-all}"

if [ "$SERVICE_NAME" = "all" ]; then
    echo -e "${BLUE}Pushing all services...${NC}\n"
    for service in "${SERVICES[@]}"; do
        push_service "$service" "$VERSION"
    done
    echo -e "${GREEN}✓ All services pushed successfully!${NC}"
else
    # Check if service exists
    if [[ ! " ${SERVICES[@]} " =~ " ${SERVICE_NAME} " ]]; then
        echo -e "${YELLOW}Error: Service '${SERVICE_NAME}' not found.${NC}"
        echo -e "Available services: ${SERVICES[*]}"
        exit 1
    fi

    push_service "$SERVICE_NAME" "$VERSION"
fi
