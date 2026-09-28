import type { components } from '../api/generated/api-schema';

export type Account = components['schemas']['AccountResponse'];
export type AccountType = NonNullable<Account['accountType']>;
export type LoginPayload = components['schemas']['LoginRequest'];
export type AuthResponse = components['schemas']['AuthResponse'];

export const ACCOUNT_TYPES = ['STUDENT', 'ADVISOR', 'ADMIN'] as const;

export const ACCOUNT_TYPE = {
  STUDENT: 'STUDENT',
  ADVISOR: 'ADVISOR',
  ADMIN: 'ADMIN',
} as const satisfies Record<string, AccountType>;
