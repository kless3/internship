FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG VITE_SERVER_URL=http://localhost:8080
ARG VITE_REQUEST_TIMEOUT_MS=15000
ENV VITE_SERVER_URL=${VITE_SERVER_URL}
ENV VITE_REQUEST_TIMEOUT_MS=${VITE_REQUEST_TIMEOUT_MS}

RUN npm run build

FROM nginx:1.27-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

