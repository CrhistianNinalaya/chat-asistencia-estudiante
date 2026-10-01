import { describe, it, expect } from 'vitest';
import { getStatusVariant } from '@/utils/statusUtils';

describe('statusUtils', () => {
  describe('getStatusVariant', () => {
    it('should return success for 2xx status codes', () => {
      // Arrange
      const statusCodes = [200, 201, 204];

      // Act & Assert
      for (const code of statusCodes) {
        expect(getStatusVariant(code)).toBe('success');
      }
    });

    it('should return error for 4xx client error status codes', () => {
      // Arrange
      const statusCodes = [400, 401, 403, 404, 422];

      // Act & Assert
      for (const code of statusCodes) {
        expect(getStatusVariant(code)).toBe('error');
      }
    });

    it('should return error for 5xx server error status codes', () => {
      // Arrange
      const statusCodes = [500, 502, 503, 504];

      // Act & Assert
      for (const code of statusCodes) {
        expect(getStatusVariant(code)).toBe('error');
      }
    });

    it('should return info for 1xx informational status codes', () => {
      // Arrange
      const statusCodes = [100, 101, 102];

      // Act & Assert
      for (const code of statusCodes) {
        expect(getStatusVariant(code)).toBe('info');
      }
    });

    it('should return info for 3xx redirection status codes', () => {
      // Arrange
      const statusCodes = [301, 302, 307];

      // Act & Assert
      for (const code of statusCodes) {
        expect(getStatusVariant(code)).toBe('info');
      }
    });

    it('should return info when statusCode is null', () => {
      // Arrange
      const statusCode = null;

      // Act
      const result = getStatusVariant(statusCode);

      // Assert
      expect(result).toBe('info');
    });

    it('should return info when statusCode is undefined', () => {
      // Arrange
      const statusCode = undefined;

      // Act
      const result = getStatusVariant(statusCode);

      // Assert
      expect(result).toBe('info');
    });

    it('should return info when statusCode is NaN', () => {
      // Arrange
      const statusCode = Number.NaN;

      // Act
      const result = getStatusVariant(statusCode);

      // Assert
      expect(result).toBe('info');
    });
  });
});
