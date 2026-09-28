import type { components } from '../api/generated/api-schema';

export type Message = components['schemas']['MessageResponse'];
export type SendMessagePayload = components['schemas']['MessageSendRequest'];
export type MessagePagedResponse = components['schemas']['MessagePagedResponse'];
export type MessageSenderRole = NonNullable<Message['senderRole']>;

export const MESSAGE_SENDER_ROLES = ['STUDENT', 'ADVISOR', 'ADMIN'] as const;

export const MESSAGE_SENDER_ROLE = {
  STUDENT: 'STUDENT',
  ADVISOR: 'ADVISOR',
  ADMIN: 'ADMIN',
} as const satisfies Record<string, MessageSenderRole>;
