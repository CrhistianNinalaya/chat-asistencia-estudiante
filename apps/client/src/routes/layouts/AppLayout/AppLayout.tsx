import { Outlet, Link } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { Badge, Button, Footer, ThemeToggle } from '@/components/ui';
import styles from './AppLayout.module.css';

export function AppLayout() {
  const { isAuthenticated, user, logout } = useAuthStore();

  const isStudent = user?.accountType === 'STUDENT';
  const roleBadgeVariant = isStudent ? 'student' : 'advisor';
  const roleLabel = isStudent ? 'Student' : 'Advisor';

  return (
    <div className={styles.layout}>
      <header className={styles.header}>
        <div className={styles.brandContainer}>
          {isAuthenticated && (
            <Link to="/" className={styles.brandLink}>
              <span className={styles.logoIcon} aria-hidden="true">💬</span>
              <span className={styles.brandTitle}>Student Support Chat</span>
            </Link>
          )}
        </div>

        <div className={styles.actionsContainer}>
          <ThemeToggle />

          {isAuthenticated && user && (
            <div className={styles.userSection}>
              <span className={styles.userName}>
                {user.firstName} {user.lastName}
              </span>
              <Badge variant={roleBadgeVariant}>{roleLabel}</Badge>
              <Button variant="outline" size="sm" onClick={logout}>
                Sign Out
              </Button>
            </div>
          )}
        </div>
      </header>

      <main className={styles.main}>
        <Outlet />
      </main>

      <Footer />
    </div>
  );
}
