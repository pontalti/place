# 🏥 Place

Place - code challenge.

---

## 🔧 Development Environment

- **OS:** Fedora 42  
- **JDK:** 21  
- **Build Tool:** Gradle 9.1.0  
- **Version Control:** Git 2.51.1  
- **API Testing Tool:** Postman and curl  

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone git@github.com:pontalti/place.git
cd place
```

Or visit the repo: https://github.com/pontalti/place

### 2. Requirements

Make sure the following are installed:

- JDK 21  
- Gradle 9.1.0  
- Your favorite IDE (VSCode, IntelliJ, Eclipse, etc.)  
- (Optional) Docker Desktop – only required if you want to run the application in containers.

---

## 🛠 Build the Project (Quarkus)

From the project root folder:

```bash
./gradlew clean build --refresh-dependencies
```

This will build the Quarkus application (fast-jar layout) under `build/quarkus-app`.

---

## ▶️ Run the Application

From the project root folder:

### Option 1 – Quarkus dev mode (recommended)

Hot reload, detailed logs, etc.:

```bash
./gradlew quarkusDev
```

The app will start on:

```text
http://localhost:8081
```

### Option 2 – via Jar (runner)

After `./gradlew build`, run:

```bash
java -jar build/quarkus-app/quarkus-run.jar
```

### Option 3 – via Container (Docker)

Build the container image(s):

```bash
docker-compose build
```

Start the containers in detached mode:

```bash
docker-compose up -d
```

Follow the application logs:

```bash
docker logs -f place
```

---

## 🌐 API Overview

The application loads initial data into an H2 in-memory database and exposes the following REST endpoints (Quarkus / RESTEasy Reactive).

> ℹ️ A aplicação está configurada para rodar na porta **8081**.

### 🏠 Home Endpoint

```bash
curl http://localhost:8081/
```

### 📍 Places

Base path dos recursos: `/place`.

#### Get all places

```bash
curl http://localhost:8081/place
```

#### Get place by ID

```bash
curl http://localhost:8081/place/1
```

#### Get grouped opening hours by place ID

```bash
curl http://localhost:8081/place/1/opening-hours/grouped
```

#### Create one or multiple places

O endpoint aceita um array de objetos Place.

```bash
curl -X POST http://localhost:8081/place   -H "Content-Type: application/json"   -d @/path/to/postman/place.json
```

#### Delete a place by ID

```bash
curl -X DELETE http://localhost:8081/place/{id}
```

#### Update a place (full update)

```bash
curl -X PUT http://localhost:8081/place/{id}   -H "Content-Type: application/json"   -d @/path/to/postman/place_update.json
```

#### Partial update (PATCH)

```bash
curl -X PATCH http://localhost:8081/place/{id}   -H "Content-Type: application/json"   -d @/path/to/postman/place_partial_update.json
```

---

## 🧪 Using Postman

You can import a predefined collection to test all endpoints easily:

```text
<PROJECT_ROOT>/postman/place.postman_collection.json
```

---

## 🧬 OpenAPI Documentation (Swagger)

With Quarkus + SmallRye OpenAPI + Swagger UI, you can access:

- Swagger UI:

  ```text
  http://localhost:8081/q/swagger-ui/
  ```

- OpenAPI document (JSON/YAML):

  ```text
  http://localhost:8081/q/openapi
  ```

---

## 🧰 Additional Tools

### Install curl (if not already available)

#### Windows

```bash
choco install curl
```

#### Linux (Debian/Ubuntu)

```bash
sudo apt-get install curl
```

#### Linux (Fedora)

```bash
sudo dnf install curl
```

---

## ✅ Running Tests

To run the Quarkus tests (including the REST tests with RestAssured):

```bash
./gradlew test
```

---