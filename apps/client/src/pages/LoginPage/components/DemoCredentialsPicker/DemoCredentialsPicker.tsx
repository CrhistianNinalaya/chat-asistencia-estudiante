import { Badge } from '@/components/ui';
import styles from './DemoCredentialsPicker.module.css';

interface DemoAccount {
  name: string;
  email: string;
  role: 'advisor' | 'student';
  roleLabel: string;
}

const DEMO_ACCOUNTS: readonly DemoAccount[] = [
  {
    name: 'Carlos Mendoza',
    email: 'asesor@demo.com',
    role: 'advisor',
    roleLabel: 'Advisor',
  },
  {
    name: 'Ana García',
    email: 'estudiante1@demo.com',
    role: 'student',
    roleLabel: 'Student',
  },
] as const;

export interface DemoCredentialsPickerProps {
  onSelect: (email: string, password: string) => void;
}

export function DemoCredentialsPicker({ onSelect }: Readonly<DemoCredentialsPickerProps>) {
  return (
    <section className={styles.container} aria-label="Demo Credentials">
      <div className={styles.header}>
        <span className={styles.title}>Demo Quick Access</span>
        <span className={styles.hint}>Click to autofill</span>
      </div>

      <div className={styles.cardsGrid}>
        {DEMO_ACCOUNTS.map((account) => (
          <button
            key={account.email}
            type="button"
            className={styles.demoCard}
            onClick={() => onSelect(account.email, '123456')}
          >
            <Badge variant={account.role}>{account.roleLabel}</Badge>
            <span className={styles.demoName}>{account.name}</span>
            <span className={styles.demoEmail}>{account.email}</span>
          </button>
        ))}
      </div>
    </section>
  );
}
