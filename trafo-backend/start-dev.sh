#!/bin/bash

# PLN Transformer Monitoring Backend - Development Startup Script

echo "🔌 Starting PLN Transformer Monitoring Backend Development Environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start PostgreSQL database
echo "🐘 Starting PostgreSQL database..."
docker-compose up -d postgres

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 5

# Check if database is ready
until docker-compose exec -T postgres pg_isready -U trafo_user -d trafo_monitoring; do
    echo "⏳ Waiting for database..."
    sleep 2
done

echo "✅ Database is ready!"

# Start Quarkus in development mode
echo "🚀 Starting Quarkus application in development mode..."
echo "📚 Access API documentation at: http://localhost:8080/q/swagger-ui"
echo "🏥 Health check at: http://localhost:8080/q/health"
echo "🛠️  Dev UI at: http://localhost:8080/q/dev"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

./mvnw compile quarkus:dev
