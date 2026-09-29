import type { components } from '@/api/generated/api-schema';

export type Ticket = components['schemas']['TicketResponse'];
export type TicketStatus = NonNullable<Ticket['status']>;
export type TicketPriority = NonNullable<Ticket['priority']>;
export type TicketCategory = NonNullable<Ticket['category']>;
export type ResolutionCategory = NonNullable<Ticket['resolutionCategory']>;

export type CreateTicketPayload = components['schemas']['TicketCreateRequest'];
export type UpdateTicketStatusPayload = components['schemas']['TicketStatusUpdateRequest'];

export const TICKET_STATUSES = [
  'OPEN',
  'ASSIGNED',
  'IN_PROGRESS',
  'WAITING_STUDENT',
  'RESOLVED',
  'CLOSED',
] as const;

export const TICKET_STATUS = {
  OPEN: 'OPEN',
  ASSIGNED: 'ASSIGNED',
  IN_PROGRESS: 'IN_PROGRESS',
  WAITING_STUDENT: 'WAITING_STUDENT',
  RESOLVED: 'RESOLVED',
  CLOSED: 'CLOSED',
} as const satisfies Record<string, TicketStatus>;

export const TICKET_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH'] as const;

export const TICKET_PRIORITY = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
} as const satisfies Record<string, TicketPriority>;

export const TICKET_CATEGORIES = [
  'GENERAL',
  'TECHNICAL',
  'BILLING',
  'FEEDBACK',
] as const;

export const TICKET_CATEGORY = {
  GENERAL: 'GENERAL',
  TECHNICAL: 'TECHNICAL',
  BILLING: 'BILLING',
  FEEDBACK: 'FEEDBACK',
} as const satisfies Record<string, TicketCategory>;

export const RESOLUTION_CATEGORIES = [
  'SYSTEM_FIX',
  'GUIDANCE_PROVIDED',
  'EXTERNAL_DEPENDENCY',
  'OTHER',
] as const;

export const RESOLUTION_CATEGORY = {
  SYSTEM_FIX: 'SYSTEM_FIX',
  GUIDANCE_PROVIDED: 'GUIDANCE_PROVIDED',
  EXTERNAL_DEPENDENCY: 'EXTERNAL_DEPENDENCY',
  OTHER: 'OTHER',
} as const satisfies Record<string, ResolutionCategory>;
