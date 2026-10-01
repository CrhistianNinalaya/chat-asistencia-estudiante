import { useAuthStore } from '@/stores/authStore';
import { Card } from '@/components/ui';

export function StudentDashboardPage() {
  const { user } = useAuthStore();

  return (
    <div style={{ padding: 'var(--space-6)', maxWidth: '960px', margin: '0 auto', width: '100%' }}>
      <header style={{ marginBottom: 'var(--space-6)' }}>
        <h1 style={{ fontSize: 'var(--font-size-2xl)', color: 'var(--color-text)' }}>Student Portal</h1>
        <p style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-sm)' }}>
          Welcome back, {user?.firstName} {user?.lastName}
        </p>
      </header>

      <Card>
        <h2 style={{ fontSize: 'var(--font-size-lg)', marginBottom: 'var(--space-2)', color: 'var(--color-text)' }}>My Support Tickets</h2>
        <p style={{ color: 'var(--color-text-muted)', fontSize: 'var(--font-size-sm)' }}>
          Ticket management and real-time chat consultation will be available in Phase 4.
        </p>
      </Card>
    </div>
  );
}
