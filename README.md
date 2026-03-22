# Skribbl Backend

Real-time multiplayer drawing-and-guessing game backend built with Spring Boot, REST APIs, and STOMP WebSockets.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Web + WebSocket (SockJS/STOMP)
- Spring Data JPA
- MySQL (runtime)
- Maven

## Features

- Create and join game rooms (private/public/random)
- Ready-up lobby flow
- Real-time game lifecycle (start game, choose word, rounds, scoring)
- Real-time drawing events (stroke add/end, undo, clear)
- Public and private chat/guess handling
- Health endpoint for deployment checks

## Prerequisites

- JDK 21+
- Maven 3.9+ (or use the included Maven Wrapper)
- MySQL database

## Configuration

The app uses these environment variables:

- `DB_USERNAME` (default: `root`)
- `DB_PASSWORD` (default: `root`)
- `SERVER_PORT` (default: `8082`)
- `FRONTEND_ORIGINS` (default: `http://localhost:5173,https://skribbl-frontend.vercel.app`)

`application.properties` currently points to a MySQL URL. Override it in your environment or in a local profile as needed.

## Run Locally

```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

App base URL (default): `http://localhost:8082`

## Run Tests

```bash
# Windows
.\mvnw.cmd test

# macOS / Linux
./mvnw test
```

## REST API

Base path: `/api/rooms`

- `POST /api/rooms` - create room
- `POST /api/rooms/{roomCode}/join` - join room by code
- `POST /api/rooms/join-random` - join random public room
- `POST /api/rooms/{roomCode}/ready` - set player ready state
- `GET /api/rooms/public` - list public lobby rooms
- `GET /api/rooms/{roomCode}` - fetch room state
- `GET /health` - health check

Example create-room payload:

```json
{
  "hostName": "Hemant",
  "settings": {
    "maxPlayers": 8,
    "rounds": 3,
    "drawTimeSeconds": 80,
    "wordChoices": 3,
    "hintsEnabled": true,
    "privateRoom": true
  }
}
```

## WebSocket API

- Endpoint: `/ws` (SockJS enabled)
- App destination prefix: `/app`
- Topic prefixes: `/topic`, `/queue`

Client send destinations:

- `/app/game.start`
- `/app/game.choose-word`
- `/app/draw.add`
- `/app/draw.end`
- `/app/draw.undo`
- `/app/draw.clear`
- `/app/chat.send`
- `/app/chat.public`
- `/app/chat.private`

Server broadcasts include:

- `/topic/rooms/{roomCode}/state`
- `/topic/rooms/{roomCode}/chat`
- `/topic/rooms/{roomCode}/drawing`
- `/topic/rooms/{roomCode}/drawing/stroke`
- `/topic/rooms/{roomCode}/guess-result`
- `/topic/rooms/{roomCode}/round-end`
- `/topic/rooms/{roomCode}/game-over`
- `/topic/users/{userId}/private`

## Project Structure

```text
src/main/java/com/hemant/skribbl
  config/        # CORS, WebSocket, initial data
  controller/    # REST + socket controllers
  dto/           # request/response payloads
  model/         # domain models
  Repo/          # JPA repositories
  Service/       # business logic
```


