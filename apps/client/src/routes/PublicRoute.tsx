import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { getDefaultDashboardRoute } from '@/routes/utils/routeRoleUtils';

export function PublicRoute() {
  const { isAuthenticated, user } = useAuthStore();

  if (isAuthenticated && user) {
    return <Navigate to={getDefaultDashboardRoute(user.accountType)} replace />;
  }

  return <Outlet />;
}
