import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { ROUTES } from '@/routes/paths';
import { getDefaultDashboardRoute, isRoleAuthorized } from '@/routes/utils/routeRoleUtils';
import type { AccountType } from '@/types';

export interface ProtectedRouteProps {
  allowedRoles?: AccountType[];
}

export function ProtectedRoute({ allowedRoles }: Readonly<ProtectedRouteProps>) {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated || !user) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  if (!isRoleAuthorized(user.accountType, allowedRoles)) {
    return <Navigate to={getDefaultDashboardRoute(user.accountType)} replace />;
  }

  return <Outlet />;
}
