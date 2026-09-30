export const ROUTES = {
  LOGIN: '/login',
  STUDENT_DASHBOARD: '/student',
  ADVISOR_DASHBOARD: '/advisor',
} as const;

export type AppRoute = typeof ROUTES[keyof typeof ROUTES];
