# Datenbank – EVA-Springbatch

## Übersicht

PostgreSQL 17 mit Flyway-Migration. Alle Tabellen liegen im Schema `public`.

## Tabellen

### source_deals

Quelltabelle für den DB-Import (Susy 2). Wird per Flyway V2 mit 10 Testdatensätzen befüllt.

| Spalte | Typ | Beschreibung |
|--------|-----|-------------|
| id | BIGSERIAL | Primary Key |
| deal_number | VARCHAR(50) | Geschäftsnummer (DB-001, DB-002...) |
| customer_name | VARCHAR(100) | Kundenname |
| amount | NUMERIC(15,2) | Betrag |
| currency | VARCHAR(3) | Währung (EUR, USD) |
| deal_date | DATE | Geschäftsdatum |
| category | VARCHAR(50) | Kategorie (TRADE, LOAN) |
| status | VARCHAR(20) | Status (NEW) |
| risk_level | VARCHAR(20) | Risikolevel |
| error_message | VARCHAR(500) | Fehlermeldung |
| created_at | TIMESTAMP | Erstellt am |
| updated_at | TIMESTAMP | Geändert am |

### staging_deals

Einheitliche Zwischentabelle nach dem Import (Job 1). Gleiche Struktur wie `source_deals`.

### processing_deals

Verarbeitungstabelle (Job 2). Gleiche Struktur. Hier werden Status, Risk-Level und Error-Message durch die Susys 3-4 gesetzt.

### output_deals

Exporttabelle (Job 3, Susy 7). Enthält nur die erfolgreich verarbeiteten Deals (Status PROCESSED).

### summary_deals

Aggregationstabelle (Susy 5).

| Spalte | Typ | Beschreibung |
|--------|-----|-------------|
| id | BIGSERIAL | Primary Key |
| category | VARCHAR(50) | Kategorie |
| risk_level | VARCHAR(20) | Risikolevel |
| deal_count | INTEGER | Anzahl Deals |
| total_amount | NUMERIC(15,2) | Gesamtsumme |
| avg_amount | NUMERIC(15,2) | Durchschnittsbetrag |
| calculated_at | TIMESTAMP | Berechnet am |

## Status-Werte

| Status | Gesetzt von | Bedeutung |
|--------|------------|-----------|
| NEW | Import (Susy 1+2) | Frisch importiert |
| VALID | Validation (Susy 3) | Alle Regeln bestanden |
| INVALID | Validation (Susy 3) | Mindestens eine Regel verletzt |
| PROCESSED | Risk Engine (Susy 4) | Risiko bewertet, fertig verarbeitet |

## Risk-Level-Werte

| Level | Regel | Bedingung |
|-------|-------|-----------|
| HIGH | HighAmountRule | Betrag > 10.000 |
| LOW | Default | Keine Regel greift |

## Flyway-Migrationen

| Version | Datei | Inhalt |
|---------|-------|--------|
| V1 | `V1__create_tables.sql` | Alle 5 Tabellen anlegen |
| V2 | `V2__insert_source_deals.sql` | 10 Testdatensätze in source_deals |

## Spring Batch Metadaten-Tabellen

Werden automatisch durch Spring Batch angelegt (`spring.batch.jdbc.initialize-schema=always`):

- `batch_job_instance`
- `batch_job_execution`
- `batch_job_execution_params`
- `batch_job_execution_context`
- `batch_step_execution`
- `batch_step_execution_context`

## Verbindungsdaten

### Dev/Test (Docker Desktop)

| Parameter | Wert |
|-----------|------|
| Host | 127.0.0.1 |
| Port | 5555 |
| Database | evadb |
| Username | evauser |
| Password | Test123 |

### Prod (WSL2)

| Parameter | Wert |
|-----------|------|
| Host | localhost |
| Port | 5432 |
| Database | evadb_prod |
| Username | evauser |
| Password | Test123 |
