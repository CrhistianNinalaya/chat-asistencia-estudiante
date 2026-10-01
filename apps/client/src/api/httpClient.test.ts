import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { httpClient, ApiError, sanitizeBaseUrl } from '@/api/httpClient';
import { useAuthStore } from '@/stores/authStore';

describe('httpClient', () => {
  const originalFetch = globalThis.fetch;
  const mockFetch = vi.fn();

  beforeEach(() => {
    globalThis.fetch = mockFetch;
    mockFetch.mockReset();
    useAuthStore.getState().logout();
    localStorage.clear();
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  const createMockResponse = (options: {
    status?: number;
    statusText?: string;
    ok?: boolean;
    contentType?: string;
    data?: unknown;
  }) => {
    const status = options.status ?? 200;
    const statusText = options.statusText ?? 'OK';
    const ok = options.ok ?? (status >= 200 && status < 300);
    const headers = new Headers();
    if (options.contentType) {
      headers.set('content-type', options.contentType);
    }

    return {
      status,
      statusText,
      ok,
      headers,
      json: async () => options.data,
      text: async () =>
        typeof options.data === 'string' ? options.data : JSON.stringify(options.data),
    };
  };

  describe('ApiError', () => {
    it('should initialize correctly with status, message, and data', () => {
      // Arrange
      const status = 404;
      const message = 'Resource not found';
      const data = { detail: 'Ticket 123 does not exist' };

      // Act
      const error = new ApiError({ status, message, data });

      // Assert
      expect(error).toBeInstanceOf(Error);
      expect(error.name).toBe('ApiError');
      expect(error.status).toBe(404);
      expect(error.message).toBe('Resource not found');
      expect(error.data).toEqual(data);
    });
  });

  describe('Endpoint and URL normalization', () => {
    it('should normalize endpoint without leading slash', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { success: true },
        })
      );

      // Act
      await httpClient.get('tickets');

      // Assert
      expect(mockFetch).toHaveBeenCalledTimes(1);
      const [url] = mockFetch.mock.calls[0];
      expect(url).toMatch(/\/tickets$/);
    });

    it('should maintain endpoint with leading slash', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { success: true },
        })
      );

      // Act
      await httpClient.get('/tickets');

      // Assert
      expect(mockFetch).toHaveBeenCalledTimes(1);
      const [url] = mockFetch.mock.calls[0];
      expect(url).toMatch(/\/tickets$/);
    });
  });

  describe('Authentication header', () => {
    it('should include Authorization header when token exists in auth store', async () => {
      // Arrange
      useAuthStore.setState({ token: 'mock-jwt-token', isAuthenticated: true });
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { success: true },
        })
      );

      // Act
      await httpClient.get('/tickets');

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.headers).toHaveProperty('Authorization', 'Bearer mock-jwt-token');
    });

    it('should not include Authorization header when token is null', async () => {
      // Arrange
      useAuthStore.setState({ token: null, isAuthenticated: false });
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { success: true },
        })
      );

      // Act
      await httpClient.get('/tickets');

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.headers).not.toHaveProperty('Authorization');
    });
  });

  describe('Request body and custom headers', () => {
    it('should serialize body to JSON and set Content-Type header', async () => {
      // Arrange
      const payload = { title: 'Network issue', description: 'Cannot connect to WiFi' };
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { id: 1, ...payload },
        })
      );

      // Act
      await httpClient.post({ endpoint: '/tickets', body: payload });

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.headers).toHaveProperty('Content-Type', 'application/json');
      expect(config.headers).toHaveProperty('Accept', 'application/json');
      expect(config.body).toBe(JSON.stringify(payload));
    });

    it('should merge custom headers with default headers', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { success: true },
        })
      );

      // Act
      await httpClient.get('/tickets', {
        headers: { 'X-Custom-Trace-Id': 'trace-123' },
      });

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.headers).toHaveProperty('Accept', 'application/json');
      expect(config.headers).toHaveProperty('X-Custom-Trace-Id', 'trace-123');
    });
  });

  describe('Response parsing', () => {
    it('should parse JSON response when content-type is application/json', async () => {
      // Arrange
      const expectedData = { id: 1, name: 'Support' };
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: expectedData,
        })
      );

      // Act
      const result = await httpClient.get<{ id: number; name: string }>('/tickets/1');

      // Assert
      expect(result).toEqual(expectedData);
    });

    it('should parse text response when content-type is not JSON', async () => {
      // Arrange
      const expectedText = 'OK text plain response';
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'text/plain',
          data: expectedText,
        })
      );

      // Act
      const result = await httpClient.get<string>('/health');

      // Assert
      expect(result).toBe(expectedText);
    });

    it('should return undefined when status is 204 No Content', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          status: 204,
          statusText: 'No Content',
          data: null,
        })
      );

      // Act
      const result = await httpClient.delete('/tickets/1');

      // Assert
      expect(result).toBeUndefined();
    });
  });

  describe('Error handling', () => {
    it('should throw ApiError with detail message if present', async () => {
      // Arrange
      const errorPayload = { detail: 'Ticket is closed' };
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          status: 400,
          statusText: 'Bad Request',
          ok: false,
          contentType: 'application/json',
          data: errorPayload,
        })
      );

      // Act & Assert
      await expect(
        httpClient.post({ endpoint: '/tickets/1/messages', body: { content: 'hello' } })
      ).rejects.toMatchObject({
        status: 400,
        message: 'Ticket is closed',
        data: errorPayload,
      });
    });

    it('should throw ApiError with message field if detail is missing', async () => {
      // Arrange
      const errorPayload = { message: 'Unauthorized access' };
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          status: 401,
          statusText: 'Unauthorized',
          ok: false,
          contentType: 'application/json',
          data: errorPayload,
        })
      );

      // Act & Assert
      await expect(httpClient.get('/tickets')).rejects.toMatchObject({
        status: 401,
        message: 'Unauthorized access',
        data: errorPayload,
      });
    });

    it('should throw ApiError with fallback message when error payload has neither detail nor message', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          status: 500,
          statusText: 'Internal Server Error',
          ok: false,
          contentType: 'application/json',
          data: {},
        })
      );

      // Act & Assert
      await expect(httpClient.get('/tickets')).rejects.toMatchObject({
        status: 500,
        message: 'Internal Server Error',
      });
    });

    it('should throw ApiError with fallback message when data is a raw string', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          status: 502,
          statusText: 'Bad Gateway',
          ok: false,
          contentType: 'text/html',
          data: '<html>Bad Gateway</html>',
        })
      );

      // Act & Assert
      await expect(httpClient.get('/tickets')).rejects.toMatchObject({
        status: 502,
        message: 'Bad Gateway',
      });
    });

    it('should parse error detail when content-type is application/problem+json', async () => {
      // Arrange
      const problemDetail = {
        title: 'Unauthorized',
        status: 401,
        detail: 'Invalid credentials',
        instance: '/api/auth/login',
      };
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          status: 401,
          statusText: 'Unauthorized',
          ok: false,
          contentType: 'application/problem+json',
          data: problemDetail,
        })
      );

      // Act & Assert
      await expect(
        httpClient.post({ endpoint: '/auth/login', body: { email: 'a@b.com', password: 'wrong' } })
      ).rejects.toMatchObject({
        status: 401,
        message: 'Invalid credentials',
        data: problemDetail,
      });
    });

    it('should extract detail from stringified JSON error payload', async () => {
      // Arrange
      const stringifiedJson = JSON.stringify({
        title: 'Unauthorized',
        status: 401,
        detail: 'Invalid credentials',
      });
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          status: 401,
          statusText: '',
          ok: false,
          contentType: 'text/plain',
          data: stringifiedJson,
        })
      );

      // Act & Assert
      await expect(
        httpClient.post({ endpoint: '/auth/login', body: { email: 'a@b.com', password: 'wrong' } })
      ).rejects.toMatchObject({
        status: 401,
        message: 'Invalid credentials',
        data: {
          title: 'Unauthorized',
          status: 401,
          detail: 'Invalid credentials',
        },
      });
    });

    it('should format fallback error without trailing colon when statusText is empty', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          status: 401,
          statusText: '',
          ok: false,
          contentType: 'text/html',
          data: '<html>401</html>',
        })
      );

      // Act & Assert
      await expect(httpClient.get('/tickets')).rejects.toMatchObject({
        status: 401,
        message: 'An unexpected error occurred. Please try again.',
      });
    });
  });

  describe('HTTP methods delegation', () => {
    it('should delegate GET request with method GET', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: [],
        })
      );

      // Act
      await httpClient.get('/tickets');

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.method).toBe('GET');
    });

    it('should delegate POST request with method POST and body', async () => {
      // Arrange
      const body = { name: 'New Ticket' };
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { id: 1 },
        })
      );

      // Act
      await httpClient.post({ endpoint: '/tickets', body });

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.method).toBe('POST');
      expect(config.body).toBe(JSON.stringify(body));
    });

    it('should delegate PUT request with method PUT and body', async () => {
      // Arrange
      const body = { title: 'Updated Title' };
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { id: 1, title: 'Updated Title' },
        })
      );

      // Act
      await httpClient.put({ endpoint: '/tickets/1', body });

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.method).toBe('PUT');
      expect(config.body).toBe(JSON.stringify(body));
    });

    it('should delegate PATCH request with method PATCH and body', async () => {
      // Arrange
      const body = { status: 'IN_PROGRESS' };
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { id: 1, status: 'IN_PROGRESS' },
        })
      );

      // Act
      await httpClient.patch({ endpoint: '/tickets/1', body });

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.method).toBe('PATCH');
      expect(config.body).toBe(JSON.stringify(body));
    });

    it('should delegate DELETE request with method DELETE', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          status: 204,
          statusText: 'No Content',
        })
      );

      // Act
      await httpClient.delete('/tickets/1');

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.method).toBe('DELETE');
    });

    it('should include custom headers when provided as a plain object', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce(
        createMockResponse({
          contentType: 'application/json',
          data: { success: true },
        })
      );

      // Act
      await httpClient.get('/tickets', {
        headers: { 'X-Custom-Trace': 'trace-123' },
      });

      // Assert
      const [, config] = mockFetch.mock.calls[0];
      expect(config.headers['X-Custom-Trace']).toBe('trace-123');
    });
  });

  describe('sanitizeBaseUrl', () => {
    it.each([
      { input: 'http://localhost:8080/api/', expected: 'http://localhost:8080/api' },
      { input: 'https://api.example.com/v1///', expected: 'https://api.example.com/v1' },
      { input: 'https://api.example.com/v1', expected: 'https://api.example.com/v1' },
      { input: undefined, expected: '' },
      { input: '', expected: '' },
      { input: '////', expected: '' },
    ])('should sanitize "$input" to "$expected"', ({ input, expected }) => {
      // Act
      const result = sanitizeBaseUrl(input);

      // Assert
      expect(result).toBe(expected);
    });
  });
});
