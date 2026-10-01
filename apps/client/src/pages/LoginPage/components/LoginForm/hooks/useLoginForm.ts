import { useState, useEffect, type SyntheticEvent } from 'react';
import { validateEmail, validatePassword } from '../utils/authValidators';
import { useLogin } from './useLogin';

export interface UseLoginFormOptions {
  initialEmail?: string;
  initialPassword?: string;
}

export function useLoginForm({
  initialEmail = '',
  initialPassword = '',
}: UseLoginFormOptions = {}) {
  const { isLoading, serverError, serverStatus, clearServerError, login } = useLogin();

  const [email, setEmail] = useState(initialEmail);
  const [password, setPassword] = useState(initialPassword);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);

  useEffect(() => {
    if (initialEmail) {
      setEmail(initialEmail);
      setEmailError(null);
    }
    if (initialPassword) {
      setPassword(initialPassword);
      setPasswordError(null);
    }
  }, [initialEmail, initialPassword]);

  const handleEmailChange = (value: string) => {
    setEmail(value);
    if (emailError) {
      setEmailError(null);
    }
    if (serverError) {
      clearServerError();
    }
  };

  const handlePasswordChange = (value: string) => {
    setPassword(value);
    if (passwordError) {
      setPasswordError(null);
    }
    if (serverError) {
      clearServerError();
    }
  };

  const handleSubmit = async (event: SyntheticEvent<HTMLFormElement>) => {
    event.preventDefault();

    const emailValidation = validateEmail(email);
    const passwordValidation = validatePassword(password);

    setEmailError(emailValidation);
    setPasswordError(passwordValidation);

    if (emailValidation || passwordValidation) {
      return;
    }

    await login({
      email,
      password,
    });
  };

  return {
    email,
    password,
    emailError,
    passwordError,
    serverError,
    serverStatus,
    isLoading,
    handleEmailChange,
    handlePasswordChange,
    handleSubmit,
  };
}
