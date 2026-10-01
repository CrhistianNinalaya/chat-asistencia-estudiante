import { describe, expect, it } from 'vitest';
import { cx } from './classNames';

describe('cx classNames utility', () => {
  it('should join single and multiple class strings', () => {
    // Arrange
    const classA = 'badge';
    const classB = 'badge-primary';

    // Act
    const result = cx(classA, classB);

    // Assert
    expect(result).toBe('badge badge-primary');
  });

  it('should filter out falsy values like undefined, null, false, and empty strings', () => {
    // Arrange & Act
    const result = cx('btn', undefined, false, null, '', 'btn-active');

    // Assert
    expect(result).toBe('btn btn-active');
  });

  it('should return an empty string when given only falsy values or no arguments', () => {
    // Arrange & Act
    const emptyResult = cx();
    const falsyResult = cx(undefined, false, null);

    // Assert
    expect(emptyResult).toBe('');
    expect(falsyResult).toBe('');
  });
});
