export const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\.){1,8}[a-zA-Z]{2,24}$/;


export function validateEmail(email: string): string | null {
  const trimmed = email?.trim();

  if (!trimmed) {
    return 'Email is required';
  }

  if (!EMAIL_REGEX.test(trimmed)) {
    return 'Invalid email format';
  }

  return null;
}

export function validatePassword(password: string): string | null {
  const trimmed = password?.trim();

  if (!trimmed) {
    return 'Password is required';
  }

  if (password.length < 6) {
    return 'Password must be at least 6 characters';
  }

  return null;
}
