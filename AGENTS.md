# Project Invariants & Agent Guidelines

This repository (`chat-asistencia-estudiante`) adheres to strict quality, architectural, and clean code standards. Every agent operating in this codebase must strictly enforce the following rules.

---

## 1. Clean Code & Language Standards

- **Zero Inline Comments (Production Code):** Absolutely NO inline comments (`// ...` or `/* ... */`) inside method or function bodies in production code. Code must be self-explanatory through expressive naming and clear abstractions.
- **AAA Testing Comments Exception:** In test files, explicitly delineate the structure of every test using simple section markers where each phase begins: `// Arrange`, `// Act`, and `// Assert` (or `// Act & Assert` for MockMvc chained assertions).
- **SonarQube S108 Empty Block Comment Exception:** When a block or `switch` branch is intentionally a no-op (e.g., an intentional no-op branch for a role like `ADMIN` requiring no validation), an explanatory English comment is explicitly allowed inside the block to satisfy SonarQube `java:S108` (e.g., `// Intentionally empty: admins have unrestricted read access`).
- **Strict English Everywhere:** 100% of all code must be written in English. This applies to identifiers, classes, methods, variables, exception types, exception messages, validation constraint messages, logs, tests, and git commit messages.
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
- **Immutable DTOs:** Use Java `record` for all DTOs and request/response payloads.
- **Built-in Clamping:** Use `Math.clamp(value, min, max)` for numerical bounding.

---

## 5. Architecture, Security & Domain Boundaries

- **Service Layer Decoupling:** Keep domain services independent of HTTP frameworks. Throw standard domain exceptions (`EntityNotFoundException`, `AccessDeniedException`, `IllegalArgumentException`) instead of `ResponseStatusException` wherever possible.
- **Service-Level Authorization:** Always enforce ownership and role checks inside service methods (e.g., students can only view or message their own tickets; advisors access assigned or unassigned tickets).
- **Domain Naming Consistency:** Use `ticketId` consistently across all layers, entities, repositories, and STOMP topics (e.g., `/topic/chat/{ticketId}`). Never use legacy `chatId`.
- **Reusable Abstractions:** Extract common lookup and authorization checks (such as `getTicketOrThrow`, `getAccountOrThrow`, `validateStudentOwnership`) into private helpers rather than repeating boilerplate.

---

## 6. Review & Git Workflow Invariants

- **Unstaged Review Fixes (Working Area Only):** When applying fixes resulting from code reviews (`docs/review-*.md`), NEVER automatically stage the changed files (`git add`). Fixes must remain strictly in the working directory (unstaged) so that the developer can easily inspect them in isolation via `git diff` against the staged area.
