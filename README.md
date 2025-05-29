# 🏥 Place

Place - code challenge.

---

## 🔧 Development Environment

- **OS:** Fedora 42
- **JDK:** 21
- **Build Tool:** Gradle 8.14
- **Version Control:** Git 2.49.0
- **API Testing Tool:** Postman and curl

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone git@github.com:pontalti/place.git
cd place
```

Or visit the repo: [https://github.com/pontalti/place](https://github.com/pontalti/place)

### 2. Requirements

Make sure the following are installed:

- [JDK 21](https://adoptium.net/)
- [Gradle 8.14](https://gradle.org/)
- Your favorite IDE (VSCode, IntelliJ, Eclipse, etc.)
- **(Optional)** [Podman](https://podman.io/) and [podman-compose](https://github.com/containers/podman-compose) – only
  required if you want to run the application in containers.

---

## 🛠 Build the Project

Navigate to the project root folder and run:

```bash
./gradlew clean build --refresh-dependencies
```

---

## ▶️ Run the Application

From the project root folder:

### Option 1 – via Gradle

```bash
./gradlew bootRun
```

### Option 2 – via Jar

```bash
java -jar build/libs/place.jar
```

### Option 3 – via Container (Podman)

Build the container image(s):

```bash
podman-compose build
```

Start the containers in detached mode:

```bash
podman-compose up -d
```

Follow the application logs:

```bash
podman logs -f place
```

---

## 🌐 API Overview

The application loads initial data into an **H2 in-memory database** and exposes the following REST endpoints:

### 🏠 Home Endpoint

```bash
curl http://localhost:8080/
```

### 📍 Places

#### Get all places

```bash
curl http://localhost:8080/places
```

#### Get place by ID

```bash
curl http://localhost:8080/places/1
```

#### Get grouped opening hours by place ID

```bash
curl http://localhost:8080/places/1/opening-hours/grouped
```

#### Create one or multiple places

```bash
curl -X POST http://localhost:8080/places -H "Content-Type: application/json" -d @/path/to/postman/places.json
```

#### Delete a place by ID

```bash
curl -X DELETE http://localhost:8080/places/{id}
```

#### Update a place

```bash
curl -X PUT http://localhost:8080/places/ -H "Content-Type: application/json" -d @/path/to/postman/place_update.json
```

#### Update a partial place

```bash
curl -X PATCH http://localhost:8080/places/{id} -H "Content-Type: application/json" -d @/path/to/postman/place_partial_update.json
```

> 💡 Replace `/path/to/postman/places.json` with the full path to your JSON file.

---

## 🧪 Using Postman

You can import a predefined collection to test all endpoints easily:

```text
<PROJECT_ROOT>/postman/place.postman_collection.json
```

---

## 🧬 OpenAPI Documentation (Swagger)

Access the API documentation via Swagger UI:

```text
http://localhost:8080/swagger-ui/swagger-ui/index.html
```

---

## 🧰 Additional Tools

### Install `curl` (if not already available)

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