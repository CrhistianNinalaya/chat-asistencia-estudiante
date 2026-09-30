# Project Invariants & Agent Guidelines

This repository (`chat-asistencia-estudiante`) adheres to strict quality, architectural, and clean code standards. Every agent operating in this codebase must strictly enforce the following rules.

---

## 1. Clean Code & Language Standards

- **Zero Inline Comments (Production Code):** Absolutely NO inline comments (`// ...` or `/* ... */`) inside method or function bodies in production code. Code must be self-explanatory through expressive naming and clear abstractions.
- **AAA Testing Comments Exception:** In test files, explicitly delineate the structure of every test using simple section markers where each phase begins: `// Arrange`, `// Act`, and `// Assert` (or `// Act & Assert` for MockMvc chained assertions).
- **SonarQube S108 Empty Block Comment Exception:** When a block or `switch` branch is intentionally a no-op (e.g., an intentional no-op branch for a role like `ADMIN` requiring no validation), an explanatory English comment is explicitly allowed inside the block to satisfy SonarQube `java:S108` (e.g., `// Intentionally empty: admins have unrestricted read access`).
- **Strict English Everywhere:** 100% of all code must be written in English. This applies to identifiers, classes, methods, variables, exception types, exception messages, validation constraint messages, logs, tests, and git commit messages.
- **Explicit Top-Level Imports (No Inline FQCN):** Always declare explicit `import` statements at the top of the file rather than using fully qualified class names inline within method signatures or bodies (e.g., declare `import java.util.Collections;` and call `Collections.emptyList()`, never `java.util.Collections.emptyList()`). Keep code idiomatic and readable.
- **Prefer Optional Chaining (`?.`) Over Chained Logical Expressions:** Always prefer using an optional chain expression instead, as it's more concise and easier to read. Never write chained logical AND expressions or redundant fallback checks such as `foo && foo.bar`, `!val || !val.trim()`, or `contentType && contentType.includes(...)`. Instead, always write `foo?.bar`, `!val?.trim()`, and `contentType?.includes(...)`. This invariant applies across all TypeScript files in the workspace and is strictly enforced by `@typescript-eslint/prefer-optional-chain`.
- **Object Parameter for Methods with More Than 2 Parameters:** Whenever a function, method, or constructor requires more than 2 parameters (> 2, i.e., 3 or more), declare it using a single options object with destructured properties (e.g., `function test({ a, b, c }: TestOptions)` instead of `function test(a, b, c)`). Functions or methods requiring 1 or 2 parameters may remain positional. This eliminates positional argument confusion and makes call sites clear and self-documenting.
- **Self-Documenting Code:** Rely on descriptive domain terminology rather than explanatory comments.

---

## 2. Date & Time Invariants (UTC)

- **Instant for Timestamps:** Always use `java.time.Instant` for all points in time (e.g., `sentAt`, `startedAt`, `closedAt`, pagination cursors). Never use `LocalDateTime` or `LocalDate` for temporal timestamps.
- **Guaranteed UTC Boundaries:** Preserve `spring.jpa.properties.hibernate.jdbc.time_zone=UTC`, `spring.jackson.time-zone=UTC`, and JVM default UTC (`TimeZone.setDefault(TimeZone.getTimeZone("UTC"))`) to eliminate timezone drift across JDBC/MySQL and Jackson JSON serialization.
- **Microsecond Timestamp Precision (TIMESTAMP(6)):** Every database column representing an `Instant` or timestamp must be defined with microsecond precision (`TIMESTAMP(6)`) in SQL DDL and explicitly mapped with `columnDefinition = "TIMESTAMP(6)"` in `@Column` annotations. This guarantees sub-second resolution and avoids cursor pagination collisions.

---

## 3. Database & JPA Performance Invariants

- **N+1 Query Prevention:** Every repository query that accesses `@ManyToOne` or other entity relationships (such as message author or ticket generator) MUST use `@EntityGraph(attributePaths = {...})` or `JOIN FETCH` to prevent N+1 query cascades.
- **Transaction Boundaries:** Mark read queries with `@Transactional(readOnly = true)` and write methods with `@Transactional` at the service layer.
- **Cursor-Based Pagination:** For real-time messaging, use cursor-based pagination with `sentAt < :before ORDER BY sentAt DESC` and `PageRequest.of(0, limit + 1)`:
  - Avoid executing unnecessary `COUNT(*)` queries.
  - Return `hasMore` and `nextCursor`.
  - Reverse results before returning so clients receive chronological `ASC` order.

---

## 4. Modern Java & SonarQube Rules (Java 21 / 25)

- **Rule Switch (`->`):** Use arrow syntax with pattern matching for `switch` expressions and statements.
- **No Empty Blocks (`java:S108`):** Never leave catch or branch blocks completely empty without explanation. If a branch or catch block is intentionally a no-op, always include an explanatory English comment explaining why no action is needed to satisfy SonarQube.
- **No Unused Variables (`java:S1481`):** Eliminate unread pattern variables and unused locals.
- **No Unnecessary Imports (`java:S1128`):** Maintain strictly clean imports. Proactively remove any unused, redundant, or orphaned import statements when creating or modifying files.
- **Cognitive Complexity <= 15 (`java:S3776`):** Keep method cognitive complexity strictly at or below 15. Proactively decompose multi-step logic (such as validations, authorization checks, and state transitions) into focused private helper methods.
- **No Redundant Matchers in Tests (`java:S6068`):** In Mockito `verify` or `when` invocations, pass literal or exact value arguments directly without redundant `eq(...)` wrappers unless mixed with argument matchers.
- **Final Test Fixture Fields:** In test classes, declare shared mock fixtures and static test principals as `final` (e.g., `private final UserPrincipal testUser = ...`).
- **Immutable DTOs:** Use Java `record` for all DTOs and request/response payloads.
- **Built-in Clamping:** Use `Math.clamp(value, min, max)` for numerical bounding.

---

## 5. Architecture, Security & Domain Boundaries

- **Service Layer Decoupling:** Keep domain services independent of HTTP frameworks. Throw standard domain exceptions (`EntityNotFoundException`, `AccessDeniedException`, `IllegalArgumentException`) instead of `ResponseStatusException` wherever possible.
- **Service-Level Authorization:** Always enforce ownership and role checks inside service methods (e.g., students can only view or message their own tickets; advisors access assigned or unassigned tickets).
- **Domain Naming Consistency:** Use `ticketId` consistently across all layers, entities, repositories, and STOMP topics (e.g., `/topic/tickets/{ticketId}`). Never use legacy `chatId` or `/topic/chat/...`.
- **Reusable Abstractions:** Extract common lookup and authorization checks (such as `getTicketOrThrow`, `getAccountOrThrow`, `validateStudentOwnership`) into private helpers rather than repeating boilerplate.
- **Strict Environment Variables (No Defaults in application.properties or @Value):** Never hardcode default/fallback values (such as `${VAR:default}`) in `application.properties` or Spring `@Value` annotations. All configuration values MUST be strictly sourced from environment variables (`.env`), and documented in `.env.example`. If a variable is missing, the application must fail fast at startup rather than falling back silently to hardcoded values.

---

## 6. Review & Git Workflow Invariants

- **Unstaged Review Fixes (Working Area Only):** When applying fixes resulting from code reviews (`docs/review-*.md`), NEVER automatically stage the changed files (`git add`). Fixes must remain strictly in the working directory (unstaged) so that the developer can easily inspect them in isolation via `git diff` against the staged area.

---

## 7. Contract Verification & OpenAPI Invariants (CI, Hooks & Drift Detection)

- **Client Build Isolation (Cloud CI / Static Hosts):** Cloud frontend build runners (such as Vercel, Netlify, or GitHub Actions) operate in pure Node.js environments without Java, Gradle, or MySQL. Therefore, `apps/client/src/api/generated/api-schema.ts` is committed to git as a versioned source of truth. Frontend builds and tests must NEVER rely on a live backend server at build time.
- **100% Offline Codegen:** `tools/scripts/codegen-api.ts` operates entirely offline using the schema exported by Gradle (`apps/server/build/openapi.json`). If the schema file is absent, it automatically triggers `./gradlew test --tests OpenApiContractTest` to generate it in ~1.5s, eliminating any network or HTTP port dependency.
- **Contract Drift Detection in CI & Pre-Push:** Whenever the backend changes any entity, DTO, or REST controller:
  1. The local `.husky/pre-push` hook rejects the push if backend changes modified contracts without staging the updated `api-schema.ts`.
  2. The GitHub Actions CI pipeline enforces the same check:
     - Run `./gradlew test` (which triggers `OpenApiContractTest` to export `apps/server/build/openapi.json` offline).
     - Run `pnpm codegen:api` (which generates client types from the offline schema).
     - Run `git diff --exit-code apps/client/src/api/generated/api-schema.ts` (fails the build if backend API contracts changed without committing the updated client schema).
- **Planned Deployment Topology (Primary Target):** The primary and most probable deployment targets are Vercel (Frontend) and Oracle Cloud Infrastructure / OCI Always Free (Backend). Runtime connectivity is governed strictly through client environment variables (`VITE_API_URL` and `VITE_WS_URL`) with CORS properly configured.
- **Domain Layer Isolation:** UI components, hooks, and services must NEVER import directly from `api-schema.ts` (e.g., `components['schemas']['...']`). All consuming code must import from the domain types layer in `apps/client/src/types/` (`Ticket`, `TicketStatus`, `TICKET_STATUS`), preserving decoupling and clean architecture.
- **Schema Naming Invariant:** Every backend DTO record exposed via REST must be explicitly annotated with `@Schema(name = "...")` to prevent simple-name collisions (such as multiple inner `Response` or `CreateRequest` records) during OpenAPI schema generation.

---

## 8. Frontend Testing Standards (Pure Logic & .test.ts Only)

- **Strictly Zero `.tsx` Tests:** Never create or maintain unit tests for React components (`*.test.tsx`, `*.spec.tsx`). Visual rendering tests are strictly prohibited.
- **Pure Functions & Utilities Only (`*.test.ts`):** Automated frontend testing is strictly reserved for pure business logic, validators, formatters, and utility functions in TypeScript (`*.test.ts`).
- **Colocalized Sibling Tests:** Every utility or pure domain logic file (e.g. `[name].ts`) in `utils/` must have its sibling test file `[name].test.ts` alongside it.
- **Vitest Scope Locking:** The test runner configuration in `apps/client/vite.config.mts` is permanently locked to `include: ['src/**/*.test.ts']`.

