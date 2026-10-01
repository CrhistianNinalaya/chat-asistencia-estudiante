import { Input, Button, Alert } from '@/components/ui';
import { useLoginForm } from './hooks/useLoginForm';
import styles from './LoginForm.module.css';

export interface LoginFormProps {
  initialEmail?: string;
  initialPassword?: string;
}

export function LoginForm({ initialEmail = '', initialPassword = '' }: Readonly<LoginFormProps>) {
  const {
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
  } = useLoginForm({ initialEmail, initialPassword });

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      {serverError ? (
        <Alert status={serverStatus} message={serverError} />
      ) : null}

      <div className={styles.fields}>
        <Input
          label="Email Address"
          type="email"
          name="email"
          autoComplete="email"
          placeholder="name@university.edu"
          value={email}
          onChange={(e) => handleEmailChange(e.target.value)}
          error={emailError}
          disabled={isLoading}
        />

        <Input
          label="Password"
          type="password"
          name="password"
          autoComplete="current-password"
          placeholder="••••••••"
          value={password}
          onChange={(e) => handlePasswordChange(e.target.value)}
          error={passwordError}
          disabled={isLoading}
        />
      </div>

      <div className={styles.submitWrapper}>
        <Button
          type="submit"
          variant="primary"
          size="lg"
          fullWidth
          isLoading={isLoading}
        >
          Sign In
        </Button>
      </div>
    </form>
  );
}
