type ClassValue = string | number | boolean | undefined | null;

export function cx(...classes: ClassValue[]): string {
  return classes.filter(Boolean).join(' ');
}
