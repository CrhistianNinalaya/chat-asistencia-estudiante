import { httpClient } from '@/api/httpClient';
import type { AuthResponse, LoginPayload } from '@/types';

export const authApi = {
  login: (payload: LoginPayload): Promise<AuthResponse> =>
    httpClient.post<AuthResponse>({
      endpoint: '/auth/login',
      body: payload,
    }),

  logout: (): Promise<void> =>
    httpClient.post<void>({
      endpoint: '/auth/logout',
    }),
};
