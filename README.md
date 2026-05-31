# Tipovačka

A sports score-guessing web application. Users submit their predictions for sport matches (e.g. IIHF Ice Hockey World Championship), earn points based on accuracy, and compete on a shared leaderboard.

---

## Features

- Submit score guesses for individual matches and predict the tournament winner
- Automatic point calculation when results are entered by an admin
- Leaderboard with gold/silver/bronze highlighting and tie-aware ranking
- Support for multiple simultaneous competitions
- Admin panel for managing competitions, teams, matches, and results
- No user accounts — participants identify by a self-chosen nickname

---

## Technology Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Backend framework | Quarkus 3.x |
| Persistence | Hibernate ORM with Panache (Quarkus extension) |
| Database | H2 (file-based, default) / PostgreSQL (production) |
| Frontend | Vaadin 24 Flow (server-side Java UI) |
| Build | Maven |

---

## Project Structure

### Domain (`sk.tipovacka.domain`)

| Class | Description |
|---|---|
| `Competition` | A sporting event (e.g. IIHF WC 2026). Has a submission deadline, a list of teams and matches, and optionally the actual tournament winner. |
| `Team` | A team participating in a specific competition. |
| `Match` | A single match within a competition between a home and away team. Stores the final result once entered by admin. |
| `Participant` | A person who submits guesses, identified by a unique nickname. |
| `CompetitionGuess` | One participant's full set of guesses for a competition — includes tournament winner prediction and cached total points. |
| `MatchGuess` | A participant's predicted score for a single match. Stores computed points once the real result is known. |
| `MatchOutcome` | Enum: `HOME_WIN`, `DRAW`, `AWAY_WIN`. |

### Services (`sk.tipovacka.service`)

| Class | Description |
|---|---|
| `ScoringService` | Calculates points for a match guess and for the tournament winner prediction. Values are read from `application.properties`. |
| `GuessService` | Handles guess submission (creates/updates `CompetitionGuess` and `MatchGuess`) and recalculates all points when a result changes. |
| `CompetitionService` | Admin operations: create competitions, add/remove teams, add matches, set match results, set tournament winner. All write operations load entities by ID inside a `@Transactional` boundary to avoid detached-entity issues. |

### Configuration (`sk.tipovacka.config`)

| Class | Description |
|---|---|
| `ScoringConfig` | `@ConfigMapping` interface that reads scoring point values from `application.properties`. |

### UI (`sk.tipovacka.ui`)

| Class | Description |
|---|---|
| `MainLayout` | App shell with sidebar navigation (Vaadin `AppLayout`). |
| `GuessView` | `/` — Competition selector and guess submission form. Shows all active competitions. Locks the form after the submission deadline. |
| `LeaderboardView` | `/leaderboard` — Competition selector (all competitions, including finished) and ranked results table with gold/silver/bronze row colours. Includes per-match guess detail. |
| `admin/AdminView` | `/admin` — Manage competitions, teams, matches, enter results and set the tournament winner. |

---

## Scoring Rules

Configurable in `application.properties` under the `tipovacka.scoring.*` prefix:

| Outcome | Default points | Property |
|---|---|---|
| Exact score | 2 | `tipovacka.scoring.exact-score` |
| Correct outcome (win / draw) | 1 | `tipovacka.scoring.correct-outcome` |
| Correct tournament winner | 2 | `tipovacka.scoring.tournament-winner` |

Note: exact score and correct outcome points **stack** — a correct exact score awards 3 points total (2 + 1).

---

## Running Locally

### Prerequisites

- Java 21+
- Maven 3.9+

### Start in dev mode

```bash
mvn quarkus:dev
```

The application starts at **http://localhost:8080**.

Dev mode uses an **H2 file-based database** — data is stored in `./data/tipovacka.mv.db` and survives restarts. On the very first run the schema is created automatically by Hibernate.

### Seed data

`src/main/resources/import.sql` contains sample data (one competition, teams, matches). It only runs when `quarkus.hibernate-orm.database.generation=drop-and-create` is set. By default (`update`) it is skipped — use the Admin page to enter your data.

To reset to seed data, temporarily change `application.properties`:
```properties
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.sql-load-script=import.sql
```
Then restart once, then revert both lines.

### Optional: run against PostgreSQL

Start a local PostgreSQL container:
```bash
docker-compose up -d
```

Then run with the production profile:
```bash
mvn quarkus:dev -Dquarkus.profile=prod
```

---

## Building a Production JAR

```bash
mvn package -Dquarkus.package.jar.type=uber-jar
java -jar target/tipovacka-1.0.0-SNAPSHOT-runner.jar
```

The resulting JAR is fully self-contained and runs anywhere Java 21 is available.
