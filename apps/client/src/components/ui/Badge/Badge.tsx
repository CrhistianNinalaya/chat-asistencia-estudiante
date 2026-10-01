import { type HTMLAttributes, type ReactNode } from 'react';
import type { AccountType } from '@/types';
import { cx } from '@/utils/classNames';
import styles from './Badge.module.css';

export type BadgeRoleVariant = Lowercase<AccountType>;
export type BadgeVariant = BadgeRoleVariant | 'primary' | 'neutral';

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  children: ReactNode;
  variant?: BadgeVariant;
  className?: string;
}

export function Badge({ children, variant = 'neutral', className = '', ...props }: Readonly<BadgeProps>) {
  const normalizedVariant = variant.toLowerCase() as Lowercase<BadgeVariant>;
  const badgeClassName = cx(styles.badge, styles[normalizedVariant], className);

  return (
    <span className={badgeClassName} {...props}>
      {children}
    </span>
  );
}
