import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '@/api/endpoints';
import { ApiError } from '@/api/httpClient';
import { useAuthStore } from '@/stores/authStore';
import { getDefaultDashboardRoute } from '@/routes/utils/routeRoleUtils';
import { ROUTES } from '@/routes/paths';
import type { LoginPayload } from '@/types';

export function useAuth() {
  const navigate = useNavigate();
  const { token, user, isAuthenticated, setAuth, logout: clearLocalAuth } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);
  const [serverStatus, setServerStatus] = useState<number | null>(null);

  const clearServerError = () => {
    setServerError(null);
    setServerStatus(null);
  };

  const login = async ({ email, password }: LoginPayload): Promise<boolean> => {
    setIsLoading(true);
    setServerError(null);
    setServerStatus(null);

    try {
      const response = await authApi.login({
        email: email.trim(),
        password,
      });

      setAuth(response);

      const targetPath = getDefaultDashboardRoute(response.user?.accountType);
      navigate(targetPath, { replace: true });
      return true;
    } catch (error: unknown) {
      if (error instanceof ApiError) {
        setServerError(error.message);
        setServerStatus(error.status);
      } else {
        setServerError('Unable to connect to the authentication service');
        setServerStatus(500);
      }
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async (): Promise<void> => {
    setIsLoading(true);
    try {
      await authApi.logout();
    } catch {
      // Intentionally empty: clear local session even if backend logout request fails
    } finally {
      clearLocalAuth();
      setIsLoading(false);
      navigate(ROUTES.LOGIN, { replace: true });
    }
  };

  return {
    token,
    user,
    isAuthenticated,
    isLoading,
    serverError,
    serverStatus,
    clearServerError,
    login,
    logout,
  };
}
