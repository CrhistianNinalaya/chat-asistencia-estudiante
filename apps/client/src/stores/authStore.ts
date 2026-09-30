import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { Account, AuthResponse } from '@/types';

interface AuthState {
  token: string | null;
  user: Account | null;
  isAuthenticated: boolean;
  setAuth: (response: AuthResponse) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      isAuthenticated: false,
      setAuth: (response: AuthResponse) => {
        set({
          token: response.token ?? null,
          user: response.user ?? null,
          isAuthenticated: Boolean(response.token),
        });
      },
      logout: () => {
        set({
          token: null,
          user: null,
          isAuthenticated: false,
        });
      },
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
    }
  )
);
