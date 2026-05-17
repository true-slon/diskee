# Stage 1: Build the React application
FROM node:18-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci && npm cache clean --force

COPY . .
RUN npm run build

# Stage 2: Serve the built app with Nginx
FROM nginx:stable-alpine

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
