import { forwardRef, useId, type InputHTMLAttributes } from 'react';
import { cx } from '@/utils/classNames';
import styles from './Input.module.css';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string | null;
  helperText?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { label, error, helperText, id: explicitId, className = '', disabled, ...props },
  ref
) {
  const generatedId = useId();
  const inputId = explicitId ?? generatedId;
  const errorId = `${inputId}-error`;
  const helperId = `${inputId}-helper`;

  const inputClassName = cx(styles.input, error && styles.inputError, className);

  let describedBy: string | undefined;
  if (error) {
    describedBy = errorId;
  } else if (helperText) {
    describedBy = helperId;
  }

  const renderFeedback = () => {
    if (error) {
      return (
        <span id={errorId} className={styles.errorMessage} role="alert">
          {error}
        </span>
      );
    }
    if (helperText) {
      return (
        <span id={helperId} className={styles.helperText}>
          {helperText}
        </span>
      );
    }
    return null;
  };

  return (
    <div className={styles.container}>
      {label ? (
        <label htmlFor={inputId} className={styles.label}>
          {label}
        </label>
      ) : null}

      <div className={styles.inputWrapper}>
        <input
          ref={ref}
          id={inputId}
          disabled={disabled}
          aria-invalid={Boolean(error)}
          aria-describedby={describedBy}
          className={inputClassName}
          {...props}
        />
      </div>

      {renderFeedback()}
    </div>
  );
});
