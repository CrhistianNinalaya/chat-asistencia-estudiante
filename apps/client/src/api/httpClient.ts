import { useAuthStore } from '@/stores/authStore';

export interface ApiErrorOptions {
  status: number;
  message: string;
  data?: unknown;
}

export class ApiError extends Error {
  public readonly status: number;
  public readonly data?: unknown;

  constructor({ status, message, data }: ApiErrorOptions) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

interface RequestOptions extends Omit<RequestInit, 'body' | 'headers'> {
  body?: unknown;
  headers?: Record<string, string>;
}

export interface HttpPayloadOptions extends RequestOptions {
  endpoint: string;
}

export function sanitizeBaseUrl(url = ''): string {
  let sanitized = url;
  while (sanitized.endsWith('/')) {
    sanitized = sanitized.slice(0, -1);
  }
  return sanitized;
}

const API_BASE_URL = sanitizeBaseUrl(import.meta.env.VITE_API_URL);

interface ParseErrorMessageOptions {
  data: unknown;
  statusText: string;
}

function tryParseJson(value: unknown): unknown {
  if (typeof value !== 'string') {
    return value;
  }
  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
}

function parseErrorMessage({ data, statusText }: ParseErrorMessageOptions): string {
  const payload = tryParseJson(data);

  if (typeof payload === 'object' && payload !== null) {
    const errorPayload = payload as Record<string, unknown>;
    if (typeof errorPayload.detail === 'string') {
      return errorPayload.detail;
    }
    if (typeof errorPayload.message === 'string') {
      return errorPayload.message;
    }
  }

  if (
    typeof payload === 'string' &&
    payload.trim().length > 0 &&
    !payload.trim().startsWith('<')
  ) {
    return payload.trim();
  }

  return statusText || 'An unexpected error occurred. Please try again.';
}

async function request<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
  const { body, headers: customHeaders, ...customOptions } = options;
  const token = useAuthStore.getState().token;

  const headers: Record<string, string> = {
    Accept: 'application/json',
    ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...customHeaders,
  };

  const config: RequestInit = {
    ...customOptions,
    headers,
    ...(body !== undefined && { body: JSON.stringify(body) }),
  };

  const normalizedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
  const response = await fetch(`${API_BASE_URL}${normalizedEndpoint}`, config);

  if (response.status === 204) {
    return undefined as unknown as T;
  }

  const contentType = response.headers.get('content-type');
  const isJson = Boolean(contentType?.includes('json'));
  const rawData = isJson ? await response.json() : await response.text();
  const data = tryParseJson(rawData);

  if (!response.ok) {
    const errorMessage = parseErrorMessage({
      data,
      statusText: response.statusText,
    });
    throw new ApiError({
      status: response.status,
      message: errorMessage,
      data,
    });
  }

  return data as T;
}

export const httpClient = {
  get: <T>(endpoint: string, options?: RequestOptions): Promise<T> =>
    request<T>(endpoint, { ...options, method: 'GET' }),

  post: <T>({ endpoint, body, ...options }: HttpPayloadOptions): Promise<T> =>
    request<T>(endpoint, { ...options, method: 'POST', body }),

  put: <T>({ endpoint, body, ...options }: HttpPayloadOptions): Promise<T> =>
    request<T>(endpoint, { ...options, method: 'PUT', body }),

  patch: <T>({ endpoint, body, ...options }: HttpPayloadOptions): Promise<T> =>
    request<T>(endpoint, { ...options, method: 'PATCH', body }),

  delete: <T>(endpoint: string, options?: RequestOptions): Promise<T> =>
    request<T>(endpoint, { ...options, method: 'DELETE' }),
};
