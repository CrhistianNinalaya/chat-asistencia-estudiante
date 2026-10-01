import { type OutputHTMLAttributes, type ReactNode } from 'react';
import { cx } from '@/utils/classNames';
import { getStatusVariant, type StatusVariant } from '@/utils/statusUtils';
import styles from './Alert.module.css';

export interface AlertProps extends OutputHTMLAttributes<HTMLOutputElement> {
  status?: number | null;
  variant?: StatusVariant;
  message?: string;
  children?: ReactNode;
  className?: string;
}

const ALERT_ICONS: Record<StatusVariant, string> = {
  error: '⚠️',
  success: '✓',
  info: 'ℹ️',
};

export function Alert({
  status,
  variant,
  message,
  children,
  className = '',
  ...props
}: Readonly<AlertProps>) {
  const resolvedVariant = variant ?? getStatusVariant(status);
  const content = children ?? message;

  if (!content) {
    return null;
  }

  const alertClassName = cx(styles.alert, styles[resolvedVariant], className);

  const icon = ALERT_ICONS[resolvedVariant];

  return (
    <output
      aria-live="polite"
      className={alertClassName}
      {...props}
    >
      <span className={styles.icon} aria-hidden="true">
        {icon}
      </span>
      <span className={styles.content}>{content}</span>
    </output>
  );
}
