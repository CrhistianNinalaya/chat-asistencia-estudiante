import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from '@/routes/ProtectedRoute';
import { PublicRoute } from '@/routes/PublicRoute';
import { ROUTES } from '@/routes/paths';
import { getDefaultDashboardRoute } from '@/routes/utils/routeRoleUtils';
import { useAuthStore } from '@/stores/authStore';
import { LoadingSpinner } from '@/components/ui';
import { AppLayout } from '@/routes/layouts/AppLayout/AppLayout';


const LoginPage = lazy(() =>
  import('@/pages/LoginPage/LoginPage').then((module) => ({ default: module.LoginPage }))
);
const StudentDashboardPage = lazy(() =>
  import('@/pages/StudentDashboardPage/StudentDashboardPage').then((module) => ({
    default: module.StudentDashboardPage,
  }))
);
const AdvisorDashboardPage = lazy(() =>
  import('@/pages/AdvisorDashboardPage/AdvisorDashboardPage').then((module) => ({
    default: module.AdvisorDashboardPage,
  }))
);

function IndexRedirect() {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated || !user) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  return <Navigate to={getDefaultDashboardRoute(user.accountType)} replace />;
}

export function AppRouter() {
  return (
    <BrowserRouter>
      <Suspense fallback={<LoadingSpinner label="Loading view..." />}>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<IndexRedirect />} />

            <Route element={<PublicRoute />}>
              <Route path={ROUTES.LOGIN} element={<LoginPage />} />
            </Route>

            <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
              <Route path={ROUTES.STUDENT_DASHBOARD} element={<StudentDashboardPage />} />
            </Route>

            <Route element={<ProtectedRoute allowedRoles={['ADVISOR', 'ADMIN']} />}>
              <Route path={ROUTES.ADVISOR_DASHBOARD} element={<AdvisorDashboardPage />} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
