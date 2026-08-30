# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [2.0.0] - 2026-08-30

### Added

- Added an Nx-managed monorepo workspace with development, test, validation, and build targets for the Web and API applications.
- Added a Java 21 backend based on Spring Boot, Spring Security, Spring Data JPA, and Flyway.
- Added a Docker Compose environment for local integration with MySQL, Mailpit, the API, and the Web application.
- Added `scripts/dev.sh` with environment validation, secure local secret generation, infrastructure readiness checks, and frontend/backend hot reload support.
- Added a GitHub Actions CI workflow that runs linting, type checking, tests, and builds.
- Added regression tests for Web pages, authentication flows, the HTTP adapter, route authorization, and the application form.
- Added API integration tests, mail-disabled mode coverage, and SMTP adapter tests.
- Added `AGENTS.md`, `DESIGN.md`, architecture documentation, and an Egg.js-to-Spring Boot migration runbook.

### Changed

- Moved the Vue 3 application from the repository root to `apps/web` and standardized its tooling on Vite, TypeScript, Vitest, and ESLint.
- Replaced the Egg.js backend with a Spring Boot API under `apps/api`.
- Changed local development to use the Nx two-task interface; Vite provides frontend HMR, while the Java development runner recompiles and restarts the API.
- Preserved the API paths, snake_case JSON fields, and response structures required by the existing Web application, while mapping the legacy database role `Client` to the public role `Industry`.
- Moved database, JWT, and SMTP runtime configuration to environment variables and removed usable default credentials from source code.
- Moved database schema management to Flyway migrations and configured Hibernate with `ddl-auto: validate` to verify mappings.

### Security

- Stored JWTs and OTPs only as irreversible digests and revoked existing tokens after a password change.
- Stored passwords with BCrypt while supporting migration from legacy plaintext passwords and passwords that exceed BCrypt's 72-byte input limit.
- Hardened the OTP state machine with expiration, resend cooldowns, failed-attempt locking, and single-use enforcement.
- Applied equivalent computation, a shared minimum response time, and identical public responses to password-reset requests and resends for known and unknown email addresses, reducing account-enumeration risk.
- Added server-side role, account-status, and object-ownership checks; students can access only their own applications.
- Restricted external profile links to absolute `http` or `https` URLs and added safe browser navigation for new windows.
- Removed frontend console output that could expose passwords, OTPs, JWTs, reset references, or server exception details.
- Bound MySQL and Mailpit host ports to `127.0.0.1` only.

### Removed

- Removed the legacy root-level Vite, ESLint, Docker, and environment configuration in favor of application-specific and workspace-level configuration.
- Removed the legacy Egg.js service and its default database, SMTP, and JWT credential configuration.

### Migration Notes

- This is a breaking release: the repository layout, backend runtime, and local development workflow now use Nx and Java.
- Before switching an existing database, follow `docs/migration-runbook.md` to complete backups, Flyway validation, migration rehearsal, and rollback preparation.
- Database, Gmail, and JWT credentials previously present in repository history must be rotated in their respective systems; removing values from source code does not invalidate existing credentials.

## [1.0.0-beta2] - 2024-11-23

### Fixed

- Fixed a Docker build failure caused by an incorrect image filename.

## [1.0.0-beta1] - 2024-11-22

### Changed

- Updated the project name and package version in preparation for the first beta release.
