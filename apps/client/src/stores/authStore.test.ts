import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from './authStore';
import type { AuthResponse } from '@/types';

describe('useAuthStore', () => {
  const sampleAuthResponse: AuthResponse = {
    token: 'jwt-mock-token-xyz',
    user: {
      id: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      accountType: 'STUDENT',
    },
  };

  beforeEach(() => {
    localStorage.clear();
    useAuthStore.getState().logout();
  });

  it('should initialize with default unauthenticated state', () => {
    // Arrange & Act
    const state = useAuthStore.getState();

    // Assert
    expect(state.token).toBeNull();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
  });

  it('should authenticate user and store credentials when setAuth is called', () => {
    // Arrange & Act
    useAuthStore.getState().setAuth(sampleAuthResponse);

    // Assert
    const state = useAuthStore.getState();
    expect(state.token).toBe(sampleAuthResponse.token);
    expect(state.user).toEqual(sampleAuthResponse.user);
    expect(state.isAuthenticated).toBe(true);
  });

  it('should persist state to localStorage under auth-storage key', () => {
    // Arrange
    useAuthStore.getState().setAuth(sampleAuthResponse);

    // Act
    const storedValue = localStorage.getItem('auth-storage');
    expect(storedValue).not.toBeNull();

    const parsed = JSON.parse(storedValue as string) as {
      state: { token: string; user: typeof sampleAuthResponse.user; isAuthenticated: boolean };
    };

    // Assert
    expect(parsed.state.token).toBe(sampleAuthResponse.token);
    expect(parsed.state.user).toEqual(sampleAuthResponse.user);
    expect(parsed.state.isAuthenticated).toBe(true);
  });

  it('should reset authentication state and clear session on logout', () => {
    // Arrange
    useAuthStore.getState().setAuth(sampleAuthResponse);

    // Act
    useAuthStore.getState().logout();

    // Assert
    const state = useAuthStore.getState();
    expect(state.token).toBeNull();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
  });
});
