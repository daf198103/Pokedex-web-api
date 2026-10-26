# Pokedex API

RESTful API built with **Java 21 + Spring Boot 3.3**, following **Clean Architecture**.
It reads data from [PokeAPI](https://pokeapi.co/docs/v2), replicates it into a local relational store,
and lets authenticated users edit proprietary fields (localized name, region, tags).

## Quick start

```bash
# Option A - Docker (PostgreSQL + API)
docker compose up --build        # API on http://localhost:8080

# Option B - local, in-memory H2 (no database needed)
mvn spring-boot:run

# Option C - local app + Postgres from compose
docker compose up -d db
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- **Demo credentials:** `demo` / `demo1234` (seeded on startup together with 4 local Pokemon: #1, #4, #7, #25)

## Tests

```bash
mvn test       # unit + integration tests
mvn verify     # + JaCoCo report (target/site/jacoco) and 60% line-coverage gate
```

## Architecture

```
domain/          entities (records), domain exceptions            -> no framework code
application/     use cases + ports (interfaces)                   -> no Spring, no JPA, no HTTP
infrastructure/  PokeAPI adapter, JPA adapters, JWT/BCrypt, config, seeding
presentation/    REST controllers, DTOs, RFC 7807 error handler
```

Dependencies point inward only. Use cases are plain Java classes wired by `UseCaseConfig`,
so business rules are independent of the API and data-access layers and are unit-tested with mocks.

| Port (application)       | Adapter (infrastructure)                    |
|--------------------------|---------------------------------------------|
| `PokemonCatalogPort`     | `PokeApiCatalogAdapter` + cached `PokeApiClient` |
| `LocalPokemonRepository` | `LocalPokemonRepositoryAdapter` (JPA)       |
| `UserRepository`         | `UserRepositoryAdapter` (JPA)               |
| `PasswordHasher`         | `BcryptPasswordHasher`                      |
| `TokenIssuer`            | `JwtTokenService`                           |

## Endpoints

| Method | Path | Auth | Story | Description |
|--------|------|------|-------|-------------|
| POST | `/api/auth/register` | public | - | Register a user |
| POST | `/api/auth/login` | public | - | Returns a JWT |
| GET | `/api/pokemon?page=0&size=20` | public | US01 | Paginated list: sprite, category, weight, abilities |
| GET | `/api/pokemon/{idOrName}` | public | US02 | Image, stats, description, evolution tree |
| POST | `/api/pokemon/{idOrName}/sync` | JWT | US03 | Idempotent upsert into the local DB |
| GET | `/api/local/pokemon` | JWT | US04 | List local Pokemon (paginated) |
| GET | `/api/local/pokemon/{id}` | JWT | US04 | Get one (404 if missing) |
| PUT | `/api/local/pokemon/{id}` | JWT | US04 | Update `localizedName`, `region`, `tags` (400/404/409) |
| DELETE | `/api/local/pokemon/{id}` | JWT | US04 | Delete (204, 404 if missing) |

Use `Authorization: Bearer <token>` for protected routes.

Errors use RFC 7807 `ProblemDetail`: 400 validation/malformed payload, 401 bad/missing credentials,
404 missing record, 409 duplicate or version conflict, 502 PokeAPI failure.

## Design decisions

- **Terminology:** "mass" = `weight` (hectograms, PokeAPI unit), "skills" = `abilities`,
  "category" = English species genus (e.g. "Mouse Pokémon").
- **Caching:** Caffeine (1h TTL, 2000 entries) on every PokeAPI call, so list pages (1 + N calls) are cheap on repeat.
  List items are fetched concurrently with virtual threads.
- **Sync is an upsert** that refreshes PokeAPI-owned fields only; proprietary fields survive a re-sync (unit-tested).
- **Optimistic locking:** `@Version` column plus an optional `version` in the PUT body -> 409 on stale writes.
- **Lists as columns:** `abilities` and `tags` are comma-separated columns (tags are validated to exclude commas).
  Simple and portable for a small list; move to join tables if you need to query by tag.
- **Security:** stateless JWT (HS256, 60 min), BCrypt passwords, CORS limited to `app.cors.allowed-origins`.
  Public: auth, Swagger, health, `GET /api/pokemon/**`. Everything else requires a token.
- **Schema** is managed by Flyway (`src/main/resources/db/migration`). H2 (PostgreSQL mode) is the default;
  the `postgres` profile targets PostgreSQL.

## Configuration

| Variable | Default | Purpose |
|----------|---------|---------|
| `JWT_SECRET` | dev value | HMAC secret, at least 32 bytes. **Change in production.** |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Frontend origins |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | local Postgres | Used with the `postgres` profile |
| `app.seed.enabled` | `true` | Seed demo user and Pokemon |

## Next steps

Frontend (React/Vue), correlation-ID logging, Testcontainers-based Postgres integration tests, refresh tokens.
