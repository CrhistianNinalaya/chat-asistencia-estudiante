import { useState } from 'react';
import { Card } from '@/components/ui';
import { DemoCredentialsPicker } from './components/DemoCredentialsPicker/DemoCredentialsPicker';
import { LoginForm } from './components/LoginForm/LoginForm';
import styles from './LoginPage.module.css';

export function LoginPage() {
  const [selectedEmail, setSelectedEmail] = useState('');
  const [selectedPassword, setSelectedPassword] = useState('');

  const handleSelectDemoCredentials = (email: string, password: string) => {
    setSelectedEmail(email);
    setSelectedPassword(password);
  };

  return (
    <div className={styles.pageContainer}>
      <div className={styles.cardWrapper}>
        <Card elevated>
          <header className={styles.header}>
            <div className={styles.logoBadge} aria-hidden="true">
              💬
            </div>
            <h1 className={styles.title}>Student Support Chat</h1>
            <p className={styles.subtitle}>
              Sign in to manage academic consultations and real-time support
            </p>
          </header>

          <DemoCredentialsPicker onSelect={handleSelectDemoCredentials} />

          <div className={styles.divider}>
            <span>Or Enter Credentials</span>
          </div>

          <LoginForm
            initialEmail={selectedEmail}
            initialPassword={selectedPassword}
          />
        </Card>
      </div>
    </div>
  );
}
