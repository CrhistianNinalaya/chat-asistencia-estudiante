import { type ButtonHTMLAttributes } from 'react';
import { useTheme } from '@/hooks/useTheme';
import { cx } from '@/utils/classNames';
import styles from './ThemeToggle.module.css';

export interface ThemeToggleProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  className?: string;
}

export function ThemeToggle({ className = '', ...props }: Readonly<ThemeToggleProps>) {
  const { theme, toggleTheme } = useTheme();

  const buttonClassName = cx(styles.themeToggle, className);

  const label = `Switch to ${theme === 'light' ? 'dark' : 'light'} theme`;
  const icon = theme === 'light' ? '🌙' : '☀️';

  return (
    <button
      type="button"
      className={buttonClassName}
      onClick={toggleTheme}
      aria-label={label}
      title={label}
      {...props}
    >
      <span role="img" aria-hidden="true">
        {icon}
      </span>
    </button>
  );
}
