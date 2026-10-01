import { useAuthStore } from '@/stores/authStore';
import { Card } from '@/components/ui';

export function AdvisorDashboardPage() {
  const { user } = useAuthStore();

  return (
    <div style={{ padding: 'var(--space-6)', maxWidth: '960px', margin: '0 auto', width: '100%' }}>
      <header style={{ marginBottom: 'var(--space-6)' }}>
        <h1 style={{ fontSize: 'var(--font-size-2xl)', color: 'var(--color-text)' }}>Advisor Console</h1>
        <p style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-sm)' }}>
          Advisor: {user?.firstName} {user?.lastName} ({user?.email})
        </p>
      </header>

      <Card>
        <h2 style={{ fontSize: 'var(--font-size-lg)', marginBottom: 'var(--space-2)', color: 'var(--color-text)' }}>Assigned Tickets Queue</h2>
        <p style={{ color: 'var(--color-text-muted)', fontSize: 'var(--font-size-sm)' }}>
          Advisor inbox, SLA priority filters and live chat responses will be active in Phase 4.
        </p>
      </Card>
    </div>
  );
}
