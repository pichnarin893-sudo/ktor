#!/bin/bash

# Load environment variables from .env file and run the application

if [ ! -f .env ]; then
    echo "Error: .env file not found!"
    echo "Please copy .env.example to .env and configure it:"
    echo "  cp .env.example .env"
    exit 1
fi

echo "Loading environment variables from .env..."
export $(cat .env | grep -v '^#' | xargs)

echo "Starting NatJoub Auth Service..."
./gradlew run
