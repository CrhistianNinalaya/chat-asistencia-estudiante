import { type ReactNode, type HTMLAttributes } from 'react';
import { cx } from '@/utils/classNames';
import styles from './Card.module.css';

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode;
  elevated?: boolean;
  className?: string;
}

export function Card({ children, elevated = false, className = '', ...props }: Readonly<CardProps>) {
  const cardClassName = cx(styles.card, elevated && styles.elevated, className);

  return (
    <div className={cardClassName} {...props}>
      {children}
    </div>
  );
}
