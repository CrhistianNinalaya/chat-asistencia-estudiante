import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { httpClient, ApiError } from '@/api/httpClient';
import { useAuthStore } from '@/stores/authStore';
import { getDefaultDashboardRoute } from '@/routes/utils/routeRoleUtils';
import type { AuthResponse, LoginPayload } from '@/types';

export function useLogin() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
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
      const response = await httpClient.post<AuthResponse>({
        endpoint: '/auth/login',
        body: {
          email: email.trim(),
          password,
        },
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

  return {
    isLoading,
    serverError,
    serverStatus,
    clearServerError,
    login,
  };
}
