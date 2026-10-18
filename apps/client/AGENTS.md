# Frontend Guidelines (React / TypeScript / Vite)

## 1. Code & Language Standards
- **Optional Chaining (`?.`):** Always prefer `?.` over chained logical expressions (`&&`) or redundant checks (`foo?.bar`, `!val?.trim()`).
- **Functions with > 2 Parameters:** Use a single options object with destructured properties (`function test({ a, b, c }: TestOptions)`).

## 2. Contracts & Domain Layer Isolation
- **Domain Layer Isolation:** Never import directly from `api-schema.ts`. Always import and consume domain models from `src/types/` (`Ticket`, `TicketStatus`, etc.).
- **Client Build Isolation & Codegen:** `src/api/generated/api-schema.ts` is committed to git. When backend contracts change, regenerate it offline via `pnpm codegen:api`.

## 3. Testing Standards (Pure Logic & .test.ts Only)
- **Strictly Zero `.tsx` Tests:** Never create or maintain unit or rendering tests for React components (`*.test.tsx`, `*.spec.tsx`).
- **Pure Functions Only (`*.test.ts`):** Automated tests are strictly reserved for pure business logic, validators, formatters, and utilities in `utils/`.
- **Colocalized Sibling Tests:** Every test file must sit alongside its source file (`[name].test.ts`).
- **Scope Locked:** Vitest configuration is locked to `include: ['src/**/*.test.ts']`.
