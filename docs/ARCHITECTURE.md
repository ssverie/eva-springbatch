# Architektur – EVA-Springbatch

## Übersicht

EVA ist eine klassische ETL-Pipeline (Extract → Transform → Load), implementiert mit Spring Batch. Die Verarbeitung ist in 3 Jobs und 7 Subsysteme (Susys) aufgeteilt.

## Datenfluss

```
CSV-Datei ──→ ┐
               ├──→ staging_deals ──→ processing_deals ──→ output_deals
source_deals ─→ ┘                                      └──→ deals-export.xml
                                                        └──→ summary_deals
```

## Job 1 – INPUT (Extract)

Ziel: Daten aus verschiedenen Quellen einheitlich in die Staging-Tabelle bringen.

| Susy | Aufgabe | Quelle | Ziel |
|------|---------|--------|------|
| Susy 1 | CSV-Import | `data/deals.csv` | `staging_deals` |
| Susy 2 | DB-Import | `source_deals` | `staging_deals` |

Beide Steps schreiben im Status `NEW` in die Staging-Tabelle.

## Job 2 – PROCESSING (Transform)

Ziel: Fachliche Verarbeitung in drei aufeinanderfolgenden Steps.

| Susy | Aufgabe | Liest aus | Schreibt in |
|------|---------|-----------|-------------|
| Susy 3 | Validation | `staging_deals` | `processing_deals` |
| Susy 4 | Risk Engine | `processing_deals` (VALID) | `processing_deals` (Update) |
| Susy 5 | Aggregation | `processing_deals` (PROCESSED) | `summary_deals` |

### Susy 3 – Validation

Regelbasierte Prüfung über das `ValidationRule`-Interface:

- `CustomerNameRule`: Kundenname darf nicht leer/null sein
- `PositiveAmountRule`: Betrag muss positiv sein

Ergebnis: Status wird auf `VALID` oder `INVALID` gesetzt. Fehler werden in `error_message` protokolliert. Alle Datensätze (auch ungültige) werden nach `processing_deals` geschrieben.

### Susy 4 – Risk Engine

Regelbasierte Risikobewertung über das `RiskRule`-Interface:

- `HighAmountRule`: Betrag > 10.000 → `risk_level = HIGH`
- Default: `risk_level = LOW`

Nur VALID-Datensätze werden verarbeitet. Status wird auf `PROCESSED` gesetzt.

### Susy 5 – Aggregation

Tasklet-basiert (kein Chunk-Processing). Berechnet pro Kategorie und Risk-Level:

- Anzahl Deals
- Gesamtsumme
- Durchschnittsbetrag

Ergebnis wird in `summary_deals` geschrieben.

## Job 3 – OUTPUT (Load)

Ziel: Verarbeitete Daten exportieren.

| Susy | Aufgabe | Quelle | Ziel |
|------|---------|--------|------|
| Susy 6 | XML-Export | `processing_deals` (PROCESSED) | `output/deals-export.xml` |
| Susy 7 | DB-Export | `processing_deals` (PROCESSED) | `output_deals` |

Zusätzlich wird die XML-Datei in einem Nachbearbeitungs-Step formatiert (Pretty Print).

## Job-Steuerung

Die drei Jobs werden sequenziell über den `JobRunner` (ApplicationRunner) gestartet:

1. inputJob
2. processingJob
3. outputJob

Jeder Job bekommt einen Timestamp als Parameter, damit er bei jedem App-Start neu ausgeführt werden kann.

## Rule-Pattern

Sowohl Validation (Susy 3) als auch Risk Engine (Susy 4) nutzen ein Interface-basiertes Rule-Pattern:

```
ValidationRule (Interface)
├── CustomerNameRule
└── PositiveAmountRule

RiskRule (Interface)
└── HighAmountRule
```

Neue Regeln können durch Implementierung des Interfaces und `@Component`-Annotation hinzugefügt werden. Spring injiziert automatisch alle Implementierungen.

## Testdaten

- 10 Deals per SQL (Flyway V2) in `source_deals`
- 10 Deals per CSV in `data/deals.csv`
- Bewusst fehlerhafte Daten: leere Namen, negative Beträge

Erwartetes Ergebnis nach Durchlauf: 8 INVALID, 6 HIGH, 6 LOW.
