import { ROUTES } from '@/routes/paths';
import type { AccountType } from '@/types';

export function getDefaultDashboardRoute(role?: AccountType | null): string {
  if (role === 'ADVISOR' || role === 'ADMIN') {
    return ROUTES.ADVISOR_DASHBOARD;
  }
  if (role === 'STUDENT') {
    return ROUTES.STUDENT_DASHBOARD;
  }
  return ROUTES.LOGIN;
}

export function isRoleAuthorized(
  userRole?: AccountType | null,
  allowedRoles?: readonly AccountType[]
): boolean {
  if (!allowedRoles?.length) {
    return true;
  }
  return userRole ? allowedRoles.includes(userRole) : false;
}
