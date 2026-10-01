export type StatusVariant = 'info' | 'success' | 'error';

export function getStatusVariant(statusCode?: number | null): StatusVariant {
  if (typeof statusCode !== 'number' || Number.isNaN(statusCode)) {
    return 'info';
  }
  if (statusCode >= 200 && statusCode < 300) {
    return 'success';
  }
  if (statusCode >= 400 && statusCode < 600) {
    return 'error';
  }
  return 'info';
}
