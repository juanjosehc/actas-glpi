**dockerfile backend**

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/actas-glpi-1.0.0.jar app.jar

EXPOSE 8001

ENTRYPOINT ["java","-jar","app.jar"]

**dockerfile frontend**

FROM nginx:alpine

COPY . /usr/share/nginx/html

EXPOSE 80

**docker-compose.yml**

services:

  backend:
    build:
      context: ./backend

    container_name: actas-backend

    ports:
      - "8001:8001"

    env_file:
      - .env

  frontend:
    build:
      context: ./frontend

    container_name: actas-frontend

    ports:
      - "80:80"

    depends_on:
      - backend