# Diaensho Backend - Docker Deployment Guide

## Overview
This guide explains how to build, publish, and deploy the Diaensho Backend as a Docker container on Docker Hub and then deploy it as a web service on Render.

## Prerequisites
- Docker installed on your local machine
- Docker Hub account
- Render account
- Git repository access

## Step 1: Build and Test Locally

### Build the Docker image
```bash
# Navigate to your project directory
cd Diaensho-Backend

# Build the Docker image
docker build -t diaensho-backend .
```

### Test locally using Docker Compose
```bash
# Start the application
docker-compose up

# Test the health endpoint
curl http://localhost:8080/actuator/health

# Test a simple endpoint
curl http://localhost:8080/api/auth/signin
```

## Step 2: Push to Docker Hub

### Tag and push the image
```bash
# Replace 'yourusername' with your Docker Hub username
docker tag diaensho-backend yourusername/diaensho-backend:latest

# Log in to Docker Hub
docker login

# Push the image
docker push yourusername/diaensho-backend:latest

# Optional: Push with version tag
docker tag diaensho-backend yourusername/diaensho-backend:v1.0.0
docker push yourusername/diaensho-backend:v1.0.0
```

## Step 3: Deploy on Render

### Option A: Deploy from Docker Hub

1. **Log in to Render** (https://render.com)

2. **Create a New Web Service**
   - Click "New" → "Web Service"
   - Choose "Deploy an existing image from a registry"

3. **Configure the service:**
   - **Image URL**: `yourusername/diaensho-backend:latest`
   - **Service Name**: `diaensho-backend`
   - **Region**: Choose closest to your users
   - **Instance Type**: Start with "Free" or "Starter"

4. **Set Environment Variables:**
   ```
   SPRING_PROFILES_ACTIVE=prod
   DATABASE_URL=
   DATABASE_USERNAME=
   DATABASE_PASSWORD=
   GEMINI_API_KEY=
   JWT_SECRET=your_secure_jwt_secret_here
   JWT_EXPIRATION=86400000
   PORT=8080
   ```

5. **Advanced Settings:**
   - **Port**: 8080
   - **Health Check Path**: `/actuator/health`
   - **Auto-Deploy**: Yes (optional)

### Option B: Deploy from GitHub Repository

1. **Connect your GitHub repository** to Render

2. **Create a New Web Service**
   - Choose "Build and deploy from a Git repository"
   - Select your repository

3. **Configure build settings:**
   - **Environment**: Docker
   - **Dockerfile Path**: `./Dockerfile`
   - **Build Command**: (leave empty, Dockerfile handles this)
   - **Start Command**: (leave empty, Dockerfile handles this)

4. **Set the same environment variables** as in Option A

## Step 4: Security Considerations for Production

### Update sensitive information:
1. **Generate a secure JWT secret:**
   ```bash
   # Generate a secure random key
   openssl rand -base64 32
   ```

2. **Database credentials:**
   - Use environment variables instead of hardcoded values
   - Consider using Render's PostgreSQL service

3. **API Keys:**
   - Store Gemini API key securely in environment variables
   - Consider key rotation policies

## Step 5: Monitoring and Maintenance

### Health Checks
- Render will automatically use the health check endpoint: `/actuator/health`
- Monitor logs through Render dashboard

### Logging
- Access logs via Render dashboard
- Production logging configuration is optimized for performance

### Updates
```bash
# To update the deployment:
# 1. Make changes to your code
# 2. Rebuild and push to Docker Hub
docker build -t yourusername/diaensho-backend:latest .
docker push yourusername/diaensho-backend:latest

# 3. Render will auto-deploy if auto-deploy is enabled
# Or manually trigger deployment from Render dashboard
```

## Troubleshooting

### Common Issues:

1. **Database Connection Issues:**
   - Verify DATABASE_URL, USERNAME, and PASSWORD
   - Check if your database allows connections from Render's IP ranges

2. **Port Issues:**
   - Ensure PORT environment variable is set to 8080
   - Verify Render service is configured for port 8080

3. **Memory Issues:**
   - Monitor memory usage in Render dashboard
   - Adjust JAVA_OPTS if needed: `-Xmx1g -Xms512m`

4. **Health Check Failures:**
   - Verify `/actuator/health` endpoint is accessible
   - Check if Spring Actuator is properly configured

### Useful Commands:
```bash
# View container logs locally
docker logs <container_id>

# Execute commands in running container
docker exec -it <container_id> /bin/bash

# Check application health
curl https://your-render-app.onrender.com/actuator/health
```

## Environment Variables Reference

| Variable | Description | Required |
|----------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | Spring profile (set to 'prod') | Yes |
| `DATABASE_URL` | PostgreSQL connection URL | Yes |
| `DATABASE_USERNAME` | Database username | Yes |
| `DATABASE_PASSWORD` | Database password | Yes |
| `GEMINI_API_KEY` | Google Gemini API key | Yes |
| `JWT_SECRET` | JWT signing secret | Yes |
| `JWT_EXPIRATION` | JWT token expiration time | No |
| `PORT` | Application port (default: 8080) | No |

## Performance Optimization

### For production, consider:
1. **Instance sizing**: Start with Starter plan, monitor usage
2. **Database optimization**: Use connection pooling (already configured)
3. **Caching**: Consider adding Redis for session management
4. **CDN**: Use Render's CDN for static content

This completes your Docker deployment setup for Render!
