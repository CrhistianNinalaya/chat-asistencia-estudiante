# Backend & Java Guidelines (Spring Boot / JPA)

## 1. Date & Time Invariants (UTC)
- **Instant for Timestamps:** Always use `java.time.Instant` in UTC for temporal points. Never `LocalDateTime` or `LocalDate`.
- **Microsecond Precision:** Every timestamp column must be `TIMESTAMP(6)` in DDL and `@Column(columnDefinition = "TIMESTAMP(6)")`.
- **UTC Preservation:** Preserve UTC across JDBC, Jackson, and JVM default (`TimeZone.setDefault(TimeZone.getTimeZone("UTC"))`).

## 2. Database & JPA Performance
- **N+1 Prevention:** Queries accessing `@ManyToOne` or entity relations MUST use `@EntityGraph(attributePaths = {...})` or `JOIN FETCH`.
- **Transactions:** Mark read queries `@Transactional(readOnly = true)` and write methods `@Transactional` at service layer.
- **Cursor Pagination:** For messaging, use `sentAt < :before ORDER BY sentAt DESC` with `PageRequest.of(0, limit + 1)`. Avoid `COUNT(*)`. Return reversed results in chronological `ASC` order.

## 3. Java 21/25 & SonarQube Rules
- **Modern Syntax:** Use rule switch (`->`) with pattern matching. Use `Math.clamp(value, min, max)`.
- **Cognitive Complexity <= 15 (S3776):** Decompose multi-step logic into focused private helper methods.
- **Clean Code Rules:** Clean unused imports (S1128), eliminate unused variables (S1481), no empty catch/branches without explanatory comment (S108).
- **DTOs:** Immutable Java `record` for all request/response DTOs. Every REST DTO must have `@Schema(name = "...")` to prevent name collisions.
- **Testing:** Test fixtures/principals must be `final`. Literal Mockito arguments without redundant `eq(...)` (S6068).

## 4. Architecture, Security & Configuration
- **Decoupling:** Throw standard domain exceptions (`EntityNotFoundException`, `AccessDeniedException`, `IllegalArgumentException`), never `ResponseStatusException`.
- **Authorization & Domain:** Enforce role/ownership checks in services. Consistently use `ticketId` everywhere (never `chatId` or `/topic/chat/...`).
- **Strict Environment Variables:** Never use default fallback values (`${VAR:default}`) in `application.properties` or `@Value`. All configuration must strictly originate from `.env` and be documented in `.env.example`. Fail fast at startup.
