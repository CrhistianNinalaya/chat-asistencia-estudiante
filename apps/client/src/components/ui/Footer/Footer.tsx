import { type HTMLAttributes } from 'react';
import { cx } from '@/utils/classNames';
import styles from './Footer.module.css';

export interface FooterProps extends HTMLAttributes<HTMLElement> {
  className?: string;
}

export function Footer({ className = '', ...props }: Readonly<FooterProps>) {
  const footerClassName = cx(styles.footer, className);

  return (
    <footer className={footerClassName} {...props}>
      <p className={styles.text}>
        Student Support Chat — NX Monorepo Architecture Portfolio Project by{' '}
        <a
          href="https://github.com/CrhistianNinalaya"
          target="_blank"
          rel="noopener noreferrer"
          className={styles.link}
        >
          Crhistian Ninalaya
        </a>
      </p>
    </footer>
  );
}
