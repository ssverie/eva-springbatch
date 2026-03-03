# Deployment – EVA-Springbatch

## Voraussetzungen

- Java 21
- Maven
- Docker Desktop mit WSL2-Backend
- PostgreSQL (Docker oder WSL2)

## Variante 1: Lokale Entwicklung (Docker Desktop)

### 1. PostgreSQL starten

```bash
docker run --name eva-postgres \
  -e POSTGRES_USER=evauser \
  -e POSTGRES_PASSWORD=Test123 \
  -e POSTGRES_DB=evadb \
  -p 5555:5432 \
  -d postgres:17
```

> Port 5555 verwenden! Port 5432 wird durch den WSL2-wslrelay blockiert.

### 2. App starten

```bash
mvn spring-boot:run
```

### 3. Prüfen

```bash
docker exec -it eva-postgres psql -U evauser -d evadb -c "SELECT status, risk_level, COUNT(*) FROM processing_deals GROUP BY status, risk_level;"
```

## Variante 2: WSL2 Prod-Simulation

### 1. PostgreSQL in WSL2 vorbereiten

```bash
wsl
sudo -u postgres psql
```

```sql
CREATE USER evauser WITH PASSWORD 'Test123';
CREATE DATABASE evadb_prod OWNER evauser;
\q
```

### 2. JAR bauen

```bash
mvn clean package -DskipTests
```

Das JAR liegt unter `target/eva-springbatch-0.0.1-SNAPSHOT.jar`.

### 3. JAR nach WSL2 kopieren

Von Windows aus:

```bash
cp target/eva-springbatch-0.0.1-SNAPSHOT.jar \\wsl$\Ubuntu\home\sven\
```

Oder aus WSL2:

```bash
cp /mnt/c/Projekte/EVA-Springbatch/target/eva-springbatch-0.0.1-SNAPSHOT.jar ~/
```

### 4. App starten mit Prod-Profil

```bash
java -jar eva-springbatch-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 5. Prüfen

```bash
psql -U evauser -d evadb_prod -h localhost -c "SELECT status, risk_level, COUNT(*) FROM processing_deals GROUP BY status, risk_level;"
```

## Variante 3: Render Cloud (geplant)

Noch nicht implementiert. Geplanter Ablauf:

1. Docker Compose mit App + PostgreSQL
2. Render Web Service mit Docker Deployment
3. Render PostgreSQL als managed Database

## Bekannte Probleme

### WSL2 Port-Konflikt

Docker Desktop und WSL2 können auf dem gleichen Port konkurrieren. Der `wslrelay.exe`-Prozess bindet sich auf `127.0.0.1:5432` und fängt Verbindungen ab, bevor Docker (`0.0.0.0:5432`) sie bekommt. Lösung: Immer einen Nicht-Standard-Port verwenden (z.B. 5555).

### Flyway Baseline-Fehler

Wenn die Datenbank manuell verändert wurde und die Flyway-History-Tabelle fehlt, kommt der Fehler "Found non-empty schema but no schema history table". Lösung: Datenbank komplett neu aufsetzen (Docker: Container + Volume löschen) oder `spring.flyway.baseline-on-migrate=true` setzen.

### Spring Batch Job-Wiederholung

Spring Batch merkt sich abgeschlossene Jobs. Bei erneutem Start wird ein bereits COMPLETED Job nicht wiederholt. Lösung: Der `JobRunner` übergibt bei jedem Start einen neuen Timestamp als Parameter.

## Konfiguration

### application.properties (Dev)

```properties
spring.application.name=eva-springbatch
server.port=9090
spring.datasource.url=jdbc:postgresql://localhost:5555/evadb
spring.datasource.username=evauser
spring.datasource.password=Test123
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.batch.jdbc.initialize-schema=always
spring.batch.job.enabled=false
```

### application-prod.properties (Prod)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/evadb_prod
spring.datasource.username=evauser
spring.datasource.password=Test123
```
