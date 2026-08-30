# IRM architecture

IRM is a polyglot Nx monorepo. Nx is the single task entry point; Vite owns the
Vue build and Maven owns the Java build.

```text
Browser
  │  same-origin /api
  ▼
apps/web (Vue 3 + Vite)
  │  JSON/HTTP interface
  ▼
apps/api (Spring Boot)
  ├── authentication module
  ├── student application module
  ├── persistence adapters ── MySQL
  └── mail adapter ────────── SMTP (optional)
```

## Module seams

The HTTP contract is the external seam between `web` and `api`. The migration
keeps the existing endpoint paths, snake-case JSON fields, role names and the
response shapes consumed by the Vue application. Backend implementation details
do not cross this seam.

The authentication module is deliberately deep: registration, OTP lifecycle,
password hashing and legacy-password upgrade, JWT issuance, token revocation and
account-state checks sit behind one application-facing interface. Controllers
translate HTTP requests and responses; they do not implement authentication
rules.

The student application module owns application validation, upsert semantics,
ownership checks and role-based reads. Tests exercise these behaviours through
the HTTP/application interface instead of relying on entity internals.

SMTP is a true external dependency, so mail delivery is a real seam with
production, disabled-development and test adapters. Persistence repositories are
internal seams owned by the Java modules; they are not exposed through HTTP.

## Runtime and build ownership

- Nx owns task discovery, orchestration, dependency-aware execution and caching.
- Vite and `vue-tsc` own frontend compilation and type checking.
- Maven owns Java dependencies, compilation, tests and packaging.
- Flyway owns the schema for a fresh database. Existing deployments must compare
  their real schema with the migration before switching traffic.
- Nginx owns SPA fallback and the production `/api` reverse proxy.

Long-running and environment-mutating tasks (`serve`, Docker Compose) are not
cached. Deterministic checks (`build`, `test`, `typecheck`, `lint`) are cacheable.

## Security invariants

- Passwords are BCrypt hashes. Values longer than BCrypt's input limit are first
  SHA-256 pre-hashed and stored with an explicit format marker. A valid legacy
  clear-text password is upgraded during its first successful login.
- Every protected request requires a signed, unexpired JWT that is also present
  in the token table.
- A student can read and update only their own application. `Admin` and
  `Industry` users can read student applications; only those roles can list all
  students.
- OTPs use a cryptographically secure generator, expire, are throttled and lock
  after repeated failures. Email-verification codes are consumed immediately;
  a verified reset code becomes a single-use password-change authorization and
  is consumed when the password changes.
- Password-reset requests always return a fresh, short-lived signed reference;
  unknown accounts and cooldown hits have the same public response and do not
  trigger mail, preventing status/ref-stability account enumeration.
- Runtime secrets come from environment variables and are never committed.

## Compatibility decisions

- The legacy database value `Client` maps to the public role `Industry`.
- `references` in an application request maps to the legacy `reference` column.
- `internship_options` and `preferred_companies` remain JSON-encoded strings in
  responses because the current Vue form parses them with `JSON.parse`.
- The API remains on port `7001` during migration to reduce operational change.
