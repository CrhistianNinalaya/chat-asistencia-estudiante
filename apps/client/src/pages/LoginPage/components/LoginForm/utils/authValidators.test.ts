import { describe, it, expect } from 'vitest';
import { validateEmail, validatePassword } from './authValidators';


describe('authValidators', () => {
  describe('validateEmail', () => {
    it('should return error when email is empty', () => {
      // Arrange
      const emptyEmail = '';

      // Act
      const result = validateEmail(emptyEmail);

      // Assert
      expect(result).toBe('Email is required');
    });

    it('should return error when email has only whitespace', () => {
      // Arrange
      const whitespaceEmail = '   ';

      // Act
      const result = validateEmail(whitespaceEmail);

      // Assert
      expect(result).toBe('Email is required');
    });

    it('should return error when email format is invalid', () => {
      // Arrange
      const invalidEmail = 'not-an-email';

      // Act
      const result = validateEmail(invalidEmail);

      // Assert
      expect(result).toBe('Invalid email format');
    });

    it('should return error when email domain lacks a valid extension', () => {
      // Arrange
      const invalidDomain = 'user@domain';

      // Act
      const result = validateEmail(invalidDomain);

      // Assert
      expect(result).toBe('Invalid email format');
    });

    it('should return null when email is valid', () => {
      // Arrange
      const validEmail = 'student@university.edu';

      // Act
      const result = validateEmail(validEmail);

      // Assert
      expect(result).toBeNull();
    });

    it('should return null when email contains subdomain', () => {
      // Arrange
      const subdomainEmail = 'student@cs.university.edu';

      // Act
      const result = validateEmail(subdomainEmail);

      // Assert
      expect(result).toBeNull();
    });
  });

  describe('validatePassword', () => {
    it('should return error when password is empty', () => {
      // Arrange
      const emptyPassword = '';

      // Act
      const result = validatePassword(emptyPassword);

      // Assert
      expect(result).toBe('Password is required');
    });

    it('should return error when password is less than 6 characters', () => {
      // Arrange
      const shortPassword = '12345';

      // Act
      const result = validatePassword(shortPassword);

      // Assert
      expect(result).toBe('Password must be at least 6 characters');
    });

    it('should return null when password is valid', () => {
      // Arrange
      const validPassword = 'password123';

      // Act
      const result = validatePassword(validPassword);

      // Assert
      expect(result).toBeNull();
    });
  });
});
