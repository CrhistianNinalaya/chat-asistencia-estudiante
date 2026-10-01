import { describe, it, expect } from 'vitest';
import { getDefaultDashboardRoute, isRoleAuthorized } from '@/routes/utils/routeRoleUtils';
import { ROUTES } from '@/routes/paths';

describe('routeRoleUtils', () => {
  describe('getDefaultDashboardRoute', () => {
    it('should return advisor dashboard route when role is ADVISOR', () => {
      // Arrange
      const role = 'ADVISOR';

      // Act
      const result = getDefaultDashboardRoute(role);

      // Assert
      expect(result).toBe(ROUTES.ADVISOR_DASHBOARD);
    });

    it('should return advisor dashboard route when role is ADMIN', () => {
      // Arrange
      const role = 'ADMIN';

      // Act
      const result = getDefaultDashboardRoute(role);

      // Assert
      expect(result).toBe(ROUTES.ADVISOR_DASHBOARD);
    });

    it('should return student dashboard route when role is STUDENT', () => {
      // Arrange
      const role = 'STUDENT';

      // Act
      const result = getDefaultDashboardRoute(role);

      // Assert
      expect(result).toBe(ROUTES.STUDENT_DASHBOARD);
    });

    it('should return login route when role is null', () => {
      // Arrange
      const role = null;

      // Act
      const result = getDefaultDashboardRoute(role);

      // Assert
      expect(result).toBe(ROUTES.LOGIN);
    });

    it('should return login route when role is undefined', () => {
      // Arrange
      const role = undefined;

      // Act
      const result = getDefaultDashboardRoute(role);

      // Assert
      expect(result).toBe(ROUTES.LOGIN);
    });
  });

  describe('isRoleAuthorized', () => {
    it('should return true when allowedRoles is undefined', () => {
      // Arrange
      const userRole = 'STUDENT';

      // Act
      const result = isRoleAuthorized(userRole, undefined);

      // Assert
      expect(result).toBe(true);
    });

    it('should return true when allowedRoles is an empty array', () => {
      // Arrange
      const userRole = 'ADVISOR';

      // Act
      const result = isRoleAuthorized(userRole, []);

      // Assert
      expect(result).toBe(true);
    });

    it('should return true when user role matches allowed roles', () => {
      // Arrange
      const userRole = 'STUDENT';
      const allowedRoles = ['STUDENT'] as const;

      // Act
      const result = isRoleAuthorized(userRole, allowedRoles);

      // Assert
      expect(result).toBe(true);
    });

    it('should return true when user role matches one of multiple allowed roles', () => {
      // Arrange
      const userRole = 'ADMIN';
      const allowedRoles = ['ADVISOR', 'ADMIN'] as const;

      // Act
      const result = isRoleAuthorized(userRole, allowedRoles);

      // Assert
      expect(result).toBe(true);
    });

    it('should return false when user role is not included in allowed roles', () => {
      // Arrange
      const userRole = 'STUDENT';
      const allowedRoles = ['ADVISOR', 'ADMIN'] as const;

      // Act
      const result = isRoleAuthorized(userRole, allowedRoles);

      // Assert
      expect(result).toBe(false);
    });

    it('should return false when user role is null and allowed roles are specified', () => {
      // Arrange
      const userRole = null;
      const allowedRoles = ['STUDENT'] as const;

      // Act
      const result = isRoleAuthorized(userRole, allowedRoles);

      // Assert
      expect(result).toBe(false);
    });

    it('should return false when user role is undefined and allowed roles are specified', () => {
      // Arrange
      const userRole = undefined;
      const allowedRoles = ['ADVISOR'] as const;

      // Act
      const result = isRoleAuthorized(userRole, allowedRoles);

      // Assert
      expect(result).toBe(false);
    });
  });
});
