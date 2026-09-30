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
  status: number;
  statusText: string;
}

function parseErrorMessage({ data, status, statusText }: ParseErrorMessageOptions): string {
  if (typeof data === 'object' && data !== null) {
    const errorPayload = data as Record<string, unknown>;
    if (typeof errorPayload.detail === 'string') {
      return errorPayload.detail;
    }
    if (typeof errorPayload.message === 'string') {
      return errorPayload.message;
    }
  }
  return `HTTP error ${status}: ${statusText}`;
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
  const isJson = Boolean(contentType?.includes('application/json'));
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const errorMessage = parseErrorMessage({
      data,
      status: response.status,
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
