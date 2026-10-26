# Pokedex Web

Vue 3 + TypeScript frontend for the [Pokedex API](../pokedex-api) (Spring Boot).
Browse Pokémon from PokeAPI, sync them into the local database, and edit your own fields.

## Quick start

Requirements: Node 20+ and the backend running on `http://localhost:8080`.

```bash
npm install
npm run dev          # http://localhost:5173
```

The dev server proxies `/api` to the backend, so no CORS setup is needed.
To point at another backend: `VITE_DEV_API_TARGET=http://host:8080 npm run dev` (see `.env.example`).

**Demo account:** `demo` / `demo1234` (seeded by the backend, also available via the "Use demo account" button).

## Scripts

| Command | What it does |
|---|---|
| `npm run dev` | Dev server with HMR |
| `npm run build` | Type-check (`vue-tsc`) and production build into `dist/` |
| `npm run typecheck` | Type-check only |
| `npm run lint` | ESLint (Vue + TypeScript) |
| `npm test` | Vitest unit and component tests |

## Docker

```bash
docker compose up --build     # http://localhost:3000
```

The image builds the app and serves it with nginx, which also reverse-proxies `/api/` to `API_UPSTREAM`
(default in `docker-compose.yml`: the backend on the host at port 8080).

To run everything together, add this service to the backend's `docker-compose.yml`:

```yaml
  web:
    build: ../pokedex-web
    depends_on: [api]
    environment:
      API_UPSTREAM: http://api:8080
    ports:
      - "3000:80"
```

## Features by user story

| Story | Screen | Route |
|---|---|---|
| US01 Enumeration | Card grid: sprite, category, weight, abilities. URL-driven pagination, page size selector, name/number search, skeletons, error + retry | `/pokemon?page=2&size=12` |
| US02 Detail | Artwork, stats bars, description, branching evolution tree | `/pokemon/:idOrName` |
| US03 Sync | "Sync to my collection" button (signed-in users) | `/pokemon/:idOrName` |
| US04 Modify | Collection list (table on desktop, cards on mobile), edit form, delete with confirmation | `/local`, `/local/:id/edit` |
| Auth | Register, sign in, protected routes, automatic sign-out on expired token | `/login`, `/register` |

Backend error handling is mapped to the UI: **400** shows field errors under the inputs, **404** shows a not-found state,
**409** shows a "modified elsewhere, reload the latest version" banner (optimistic locking via the `version` field),
**401** with a token signs the user out and redirects to login, **502** shows a "service unavailable" state with retry.

## Architecture

```
src/
  api/          axios client (JWT interceptor, 401 handler), typed endpoints, error normalisation
  stores/       Pinia: auth session and toasts (client state only)
  router/       routes, lazy-loaded views, auth guards, page titles
  views/        one component per route
  components/   reusable UI (cards, pagination, stat bars, evolution tree, dialog, form field, toasts)
  types/        TypeScript types mirroring the backend DTOs
  utils/        formatting helpers
tests/          Vitest + Vue Test Utils
```

- **State:** server data lives in TanStack Query (caching, retries, invalidation after sync/update/delete).
  Pinia only holds the auth session and toasts. The URL holds paging state, so links are shareable and the back button works.
- **Auth:** the JWT is stored in `localStorage` (with its expiry) and attached by an axios interceptor.
  Trade-off: readable by scripts on the page, so XSS is the main risk. It is mitigated by Vue's escaping, no third-party scripts and a
  60-minute token. An HttpOnly cookie would be the hardening step.
- **Responsive and accessible:** mobile-first CSS grid, table-to-cards switch below 720px, light/dark theme, skip link,
  labelled forms with `aria-invalid`/`aria-describedby`, native `<dialog>` for confirmations, `aria-live` toasts,
  focus styles and reduced-motion support.
- **Lazy loading:** routes are code-split and card images use `loading="lazy"`.

## Notes

- Weight is shown in kg and height in m (the API returns hectograms and decimetres).
- Evolution thumbnails load from the public PokeAPI sprites repository on GitHub.
