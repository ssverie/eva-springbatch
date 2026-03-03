# EVA-Springbatch

Modernisierte EVA Batch-Architektur mit Spring Boot + Spring Batch.

## Tech-Stack

- Java 21
- Spring Boot 3.5.11
- Spring Batch
- PostgreSQL 17
- Flyway (DB-Migration)
- Docker Desktop (WSL2)
- Maven

## Schnellstart

### 1. PostgreSQL starten (Docker)

```bash
docker run --name eva-postgres -e POSTGRES_USER=evauser -e POSTGRES_PASSWORD=Test123 -e POSTGRES_DB=evadb -p 5555:5432 -d postgres:17
```

> Port 5555 statt 5432 wegen WSL2-Portkonflikt (wslrelay blockiert 5432).

### 2. App starten

```bash
mvn spring-boot:run
```

Die App startet auf Port 9090. Flyway legt die Tabellen automatisch an und befüllt die Testdaten.

### 3. Ergebnis prüfen

- DBeaver: `127.0.0.1:5555`, DB `evadb`, User `evauser`, Passwort `Test123`
- XML-Export: `output/deals-export.xml`

## Umgebungen

| Umgebung | Datenbank   | Host           | Port |
|----------|-------------|----------------|------|
| Dev/Test | evadb       | localhost      | 5555 |
| Prod     | evadb_prod  | WSL2 localhost | 5432 |

## Projektstruktur

```
src/main/java/de/svenjerie/eva/
├── api/               → REST-Controller
├── batch/             → Job- und Step-Konfigurationen
├── config/            → Spring-Konfigurationsklassen
├── domain/            → Entity-Klassen
├── repository/        → JPA Repositories
└── service/           → Business-Logik (Rules, Validation, Risk)

src/main/resources/
├── data/              → CSV-Testdaten
├── db/migration/      → Flyway SQL-Migrationen
└── application.properties
```

## Dokumentation

- [Architektur](ARCHITECTURE.md)
- [Datenbank](DATABASE.md)
- [Deployment](DEPLOYMENT.md)
