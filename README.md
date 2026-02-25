H3 Healthcare — hospital discovery system using Uber H3 hexagonal grid indexing for sub-50ms nearest-hospital search. City filtering → H3 distance sorting with full RBAC authorization.

✨ Features
✅ RBAC: USER / WORKER / ADMIN permission matrix
✅ Appointment booking with approval workflow
✅ Opaque Token auth (stateless, secure)
✅ PostgreSQL 15 + Flyway migrations
✅ Docker Compose infrastructure

🏗️ Architecture
Mobile/Web → API Gateway → Spring Boot API → PostgreSQL 16
                            ↓
                       Kafka (Phase 3 Notifications)
🚀 Quick Start
1. Clone & Prepare
bash
git clone <your-repo>
cd h3-healthcare
2. Build Application (Manual)
bash
# Build JAR
./mvnw clean package -DskipTests

# JAR available: target/h3-healthcare-0.0.1-SNAPSHOT.jar
3. Start Infrastructure
bash
# Launch PostgreSQL + Flyway
docker-compose up

# Verify: http://localhost:5432 (pgAdmin/psql)
4. Run Application
bash
# Option A: java -jar
java -jar target/h3-healthcare-0.0.1-SNAPSHOT.jar

# Option B: Maven Spring Boot
./mvnw spring-boot:run
